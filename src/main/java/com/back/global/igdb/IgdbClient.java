package com.back.global.igdb;

import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.*;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API 경로 전용 IGDB 클라이언트 (IgdbRequestExecutor 사용).
 * - connect 1s / read 3s 타임아웃 → 빠른 실패 → CB open 가속 → DB fallback
 * - 사용처: IgdbCircuitBreakerClient (사용자 요청 경로)
 * 배치 경로는 BatchIgdbClient를 사용.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbClient {
    private static final String GAMES_ENDPOINT = "/games";
    private static final String GAME_VIDEOS_ENDPOINT = "/game_videos";

    private final IgdbRequestExecutor requestExecutor;

    public List<IgdbGameSummaryDto> searchGames(String keyword, int limit) {
        String body = """
                search "%s";
                fields id,name,summary,first_release_date;
                limit %d;
                """.formatted(escape(keyword), limit);

        IgdbGameSummaryDto[] res = requestExecutor.execute(body, IgdbGameSummaryDto[].class, GAMES_ENDPOINT, "searchGames");
        return res == null ? List.of() : List.of(res);
    }

    public IgdbGameDetailDto getGameDetail(long igdbId) {
        String body = """
            fields
                id,name,summary,first_release_date,
                involved_companies.company.name,
                involved_companies.publisher,
                involved_companies.developer,
                cover.id,cover.image_id,
                genres.id,genres.name,
                platforms.id,platforms.name;
            where id = %d;
            limit 1;
        """.formatted(igdbId);

        IgdbGameDetailDto[] res = requestExecutor.execute(body, IgdbGameDetailDto[].class, GAMES_ENDPOINT, "getGameDetail");
        return firstOrNull(res);
    }

    public IgdbGameNameDto getGameName(long igdbId) {
        String body = """
            fields
                id,name;
            where id = %d;
            limit 1;
        """.formatted(igdbId);

        IgdbGameNameDto[] res = requestExecutor.execute(body, IgdbGameNameDto[].class, GAMES_ENDPOINT, "getGameName");
        return firstOrNull(res);
    }

    public IgdbVideoDto getVideoId(long igdbGameId) {
        String body = """
            fields
                video_id;
            where game = %d;
            sort id desc;
            limit 1;
        """.formatted(igdbGameId);

        IgdbVideoDto[] res = requestExecutor.execute(body, IgdbVideoDto[].class, GAME_VIDEOS_ENDPOINT, "getVideoId");
        return firstOrNull(res);
    }

    public List<Long> getSimilarGameIds(long igdbId) {
        String body = """
            fields
                similar_games;
            where id = %d;
            limit 1;
        """.formatted(igdbId);
        IgdbSimilarIdsDto[] res = requestExecutor.execute(body, IgdbSimilarIdsDto[].class, GAMES_ENDPOINT, "getSimilarGameIds");
        if (res == null || res.length == 0 || res[0].similar_games() == null) return List.of();
        return res[0].similar_games();
    }

    public List<SimilarGameResponse> getSimilarGameBriefById(List<Long> ids) {
        List<Long> picked = ids.stream().limit(30).toList();
        if (picked.isEmpty()) return List.of();

        String in = picked.stream().map(String::valueOf).collect(Collectors.joining(","));
        String body = """
                fields id, name, cover.image_id;
                where id = (%s);
                limit %d;
                """.formatted(in, picked.size());

        IgdbGameBriefDto[] res = requestExecutor.execute(body, IgdbGameBriefDto[].class, GAMES_ENDPOINT,"fetchGameBriefsByIds");
        if (res == null || res.length == 0) return List.of();

        List<SimilarGameResponse> out = new ArrayList<>(res.length);
        for (IgdbGameBriefDto d : res) {
            if (d == null) continue;
            String coverId = (d.cover() == null) ? null : d.cover().imageId();
            out.add(new SimilarGameResponse(d.id(), d.name(), coverId));
        }
        return out;
    }

    public List<IgdbPopularityPrimitiveDto> getPopularGameIds(int limit) {
        String body = """
            fields game_id,value,popularity_type;
            where popularity_type = 1;
            sort value desc;
            limit %d;
          """.formatted(limit);

        IgdbPopularityPrimitiveDto[] res = requestExecutor.execute(body, IgdbPopularityPrimitiveDto[].class,
                "/popularity_primitives", "getPopularGameIds");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbPopularGameDto> getGamesByIds(List<Long> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) return List.of();

        String in = gameIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String body = """
          fields id, name, cover.image_id, total_rating, total_rating_count;
          where id = (%s);
          limit %d;
          """.formatted(in, gameIds.size());

        IgdbPopularGameDto[] res = requestExecutor.execute(body, IgdbPopularGameDto[].class, GAMES_ENDPOINT, "getGamesByIds");
        return res == null ? List.of() : List.of(res);
    }

    public List<GameRow> fetchGamesByIds(List<Long> idsInOrder) {
        String idList = idsInOrder.stream().map(String::valueOf).collect(Collectors.joining(","));

        String body = """
                fields id,name,cover.image_id,genres.id,genres.name,platforms.id,platforms.name,first_release_date;
                where id = (%s);
                limit %d;
                """.formatted(idList, idsInOrder.size());

        GameRow[] games = requestExecutor.execute(body, GameRow[].class, GAMES_ENDPOINT, "fetchGamesByIds");

        return games == null ? List.of() : List.of(games);
    }

    public IgdbPopularGameDto getGameRating(long igdbId) {
        String body = """
          fields id, name, cover.image_id, total_rating, total_rating_count;
          where id = %d;
          limit 1;
          """.formatted(igdbId);

        IgdbPopularGameDto[] res = requestExecutor.execute(body, IgdbPopularGameDto[].class, GAMES_ENDPOINT, "getGameRating");
        return firstOrNull(res);
    }

    public List<IgdbGenreDto> fetchGenres() {
        String body = """
        fields id,name;
        limit 500;
        """;

        IgdbGenreDto[] res = requestExecutor.execute(body, IgdbGenreDto[].class, "/genres", "fetchGenres");
        return res == null ? List.of() : List.of(res);
    }

    // ── Private helpers ───────────────────────────────────────────────

    private static <T> T firstOrNull(T[] arr) {
        if (arr == null || arr.length == 0) return null;
        return arr[0];
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
