package com.back.global.igdb.service;

import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.IgdbProperties;
import com.back.global.igdb.TwitchTokenService;
import com.back.global.igdb.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IgdbPopularRightNowService {

    // popularity_type ids
    private static final int TYPE_VISITS = 1;
    private static final int TYPE_WANT = 2;
    private static final int TYPE_TWITCH_24H_WATCHED = 34;

    private final RestClient igdbRestClient;
    private final IgdbClient igdbClient;
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;

    private final ObjectMapper objectMapper;

    /**
     * 1. "Popular right now" = weighted score of (Visits, Want, watched).
     * 2. /multiquery로 한번에 3개의 popularity_primitives 가져오기
     * 3. 값을 [0..1]로 정규화 (log1p(value) / log1p(max)사용)
     * 4. score = wV*V + wW*W + wT*T
     * 5. Top N game_ids을 가지고, 인기게임 카드 정보 찾기
     */
    public List<PopularGameCardDto> popularRightNow(int topN) {
        Weights weights = new Weights(0.30, 0.20, 0.50);

        int candidateK = Math.max(300, topN * 50);

        PopularityLists lists = fetchPrimitivesViaMultiquery(candidateK);

        // 1) Build normalized maps per type: gameId -> normScore(0..1)
        Map<Long, Double> visitsNorm = normalizeLog1p(lists.visits());
        Map<Long, Double> wantNorm = normalizeLog1p(lists.want());
        Map<Long, Double> twitchNorm = normalizeLog1p(lists.twitch());

        // 2) Union of all game ids
        Set<Long> allGameIds = new HashSet<>();
        allGameIds.addAll(visitsNorm.keySet());
        allGameIds.addAll(wantNorm.keySet());
        allGameIds.addAll(twitchNorm.keySet());

        if (allGameIds.isEmpty()) return List.of();

        // 3) Weighted score
        Map<Long, Double> scoreByGameId = new HashMap<>(allGameIds.size() * 2);
        for (Long gid : allGameIds) {
            double v = visitsNorm.getOrDefault(gid, 0.0);
            double w = wantNorm.getOrDefault(gid, 0.0);
            double t = twitchNorm.getOrDefault(gid, 0.0);

            boolean hasSupport = (w >= 0.06) || (t >= 0.06);
            if (!hasSupport) continue;

            double score = weights.visits() * v + weights.want() * w + weights.twitch() * t;
            if (score > 0) scoreByGameId.put(gid, score);
        }

        // 4) Sort by score desc, take Top N
        List<Long> topIds = scoreByGameId.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();

        for (Long gid : topIds) {
            double v = visitsNorm.getOrDefault(gid, 0.0);
            double w = wantNorm.getOrDefault(gid, 0.0);
            double t = twitchNorm.getOrDefault(gid, 0.0);
            double s = scoreByGameId.getOrDefault(gid, 0.0);
            log.info("gid={} score={} (v={}, w={}, t={})", gid, s, v, w, t);
        }
        if (topIds.isEmpty()) return List.of();

        // 5) Fetch games details
        List<GameRow> games = igdbClient.fetchGamesByIds(topIds);

        // 6) Join + keep score order
        Map<Long, GameRow> gameById = games.stream()
                .collect(Collectors.toMap(GameRow::id, g -> g, (a, b) -> a));

        List<PopularGameCardDto> result = new ArrayList<>(topIds.size());
        for (Long id : topIds) {
            GameRow g = gameById.get(id);
            if (g == null) continue;

            double score = scoreByGameId.getOrDefault(id, 0.0);
            result.add(PopularGameCardDto.from(g, score));
        }

        return result;
    }

    private PopularityLists fetchPrimitivesViaMultiquery(int limitPerType) {
        String body = """
                query popularity_primitives "visits" {
                  fields game_id,value,popularity_type;
                  where popularity_type = %d;
                  sort value desc;
                  limit %d;
                };
                query popularity_primitives "want" {
                  fields game_id,value,popularity_type;
                  where popularity_type = %d;
                  sort value desc;
                  limit %d;
                };
                query popularity_primitives "twitch" {
                  fields game_id,value,popularity_type;
                  where popularity_type = %d;
                  sort value desc;
                  limit %d;
                };
                """.formatted(
                TYPE_VISITS, limitPerType,
                TYPE_WANT, limitPerType,
                TYPE_TWITCH_24H_WATCHED, limitPerType
        );

        try {
            JsonNode raw = igdbRestClient.post()
                    .uri("/multiquery")
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", props.clientId())
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (raw == null || !raw.isArray()) return new PopularityLists(List.of(), List.of(), List.of());

            // IGDB multiquery response: [{ "name": "...", "result": [ ... ] }, ...]
            List<MultiQueryBlock> blocks = objectMapper.convertValue(
                    raw, new TypeReference<List<MultiQueryBlock>>() {
                    }
            );

            List<PopularityPrimitiveRow> visits = extractBlock(blocks, "visits");
            List<PopularityPrimitiveRow> want = extractBlock(blocks, "want");
            List<PopularityPrimitiveRow> twitch = extractBlock(blocks, "twitch");

            return new PopularityLists(visits, want, twitch);
        } catch (RestClientResponseException e) {
            // You might want to wrap into your ServiceException
            throw new RuntimeException("IGDB multiquery failed: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        }
    }

    private List<PopularityPrimitiveRow> extractBlock(List<MultiQueryBlock> blocks, String name) {
        return blocks.stream()
                .filter(b -> name.equalsIgnoreCase(b.name()))
                .findFirst()
                .map(MultiQueryBlock::result)
                .orElse(List.of());
    }

    private Map<Long, Double> normalizeLog1p(List<PopularityPrimitiveRow> rows) {
        if (rows == null || rows.isEmpty()) return Map.of();

        double max = 0.0;
        for (PopularityPrimitiveRow r : rows) {
            double v = safeDouble(r.value());
            if (v > max) max = v;
        }
        if (max <= 0.0) return Map.of();

        double denom = Math.log1p(max);

        Map<Long, Double> out = new HashMap<>(rows.size() * 2);
        for (PopularityPrimitiveRow r : rows) {
            long gid = r.gameId();
            double v = safeDouble(r.value());
            if (v <= 0) continue;

            // norm in [0..1]
            double norm = Math.log1p(v) / denom;
            // clamp just in case
            if (norm < 0) norm = 0;
            if (norm > 1) norm = 1;
            out.put(gid, norm);
        }
        return out;
    }

    private double safeDouble(BigDecimal bd) {
        return bd == null ? 0.0 : bd.doubleValue();
    }

    /**
     * rows가 value desc로 이미 정렬돼 있다고 가정(IGDB 쿼리에서 sort value desc).
     * rank=1부터 시작해서 rankScore = 1/sqrt(rank)
     */
    private Map<Long, Double> toRankScoreMap(List<PopularityPrimitiveRow> rows) {
        if (rows == null || rows.isEmpty()) return Map.of();

        Map<Long, Double> out = new HashMap<>(rows.size() * 2);
        int rank = 1;
        for (PopularityPrimitiveRow r : rows) {
            long gid = r.gameId();

            // 같은 game_id가 중복 등장할 가능성은 낮지만, 방어적으로 첫 등장만 채택
            if (out.containsKey(gid)) continue;

            double score = 1.0 / Math.sqrt(rank);
            out.put(gid, score);

            rank++;
        }
        return out;
    }
}