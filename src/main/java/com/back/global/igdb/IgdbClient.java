package com.back.global.igdb;

import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.*;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.exception.IgdbApiException;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * IgdbClient: HTTP 호출만 담당
 * 엔드포인트, 헤더, 바디(query), status code 처리
 * 응답을 DTO로 역직렬화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbClient {
    private static final String GENRES_ENDPOINT = "/game_videos";
    private static final String GAMES_ENDPOINT = "/games";
    private static final String GAME_VIDEOS_ENDPOINT = "/game_videos";
    private static final int MAX_RETRIES = 3;

    private final RestClient igdbRestClient;
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;
    private final RateLimiter igdbRateLimiter;

    @PostConstruct
    public void warmUpToken() {
        try {
            tokenService.getAccessToken(); // 앱 시작할 때 토큰 미리 발급/캐시
        } catch (Exception e) {
            // 시작 시 실패해도 앱은 떠야 하니까 로그만 남기고 무시하는 패턴
            log.warn("Twitch token warm-up failed", e);
        }
    }

    // 예: 게임 검색
    public List<IgdbGameSummaryDto> searchGames(String keyword, int limit) {
        // IGDB Query Language (APICALYPSE)
        // search "elden ring"; fields id,name,summary,first_release_date,cover.url; limit 10;
        String body = """
                search "%s";
                fields id,name,summary,first_release_date;
                limit %d;
                """.formatted(escape(keyword), limit);

        IgdbGameSummaryDto[] res = postReq(body, IgdbGameSummaryDto[].class, GAMES_ENDPOINT, "searchGames");
        return res == null ? List.of() : List.of(res);
    }

    // 게임 상세 가져오기(igdbId로)
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

        IgdbGameDetailDto[] res = postReq(body, IgdbGameDetailDto[].class, GAMES_ENDPOINT, "getGameDetail");
        return firstOrNull(res);
    }

    // 게임 이름 가져오기(igdbId로)
    public IgdbGameNameDto getGameName(long igdbId) {
        String body = """
            fields
                id,name;
            where id = %d;
            limit 1;
        """.formatted(igdbId);

        IgdbGameNameDto[] res = postReq(body, IgdbGameNameDto[].class, GAMES_ENDPOINT, "getGameName");
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

        IgdbVideoDto[] res = postReq(body, IgdbVideoDto[].class, GAME_VIDEOS_ENDPOINT, "getVideoId");
        return firstOrNull(res);
    }

    public List<Long> getSimilarGameIds(long igdbId) {
        String body = """
            fields
                similar_games;
            where id = %d;
            limit 1;
        """.formatted(igdbId);
        IgdbSimilarIdsDto[] res = postReq(body, IgdbSimilarIdsDto[].class, GAMES_ENDPOINT, "getSimilarGameIds");
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

        IgdbGameBriefDto[] res = postReq(body, IgdbGameBriefDto[].class, GAMES_ENDPOINT,"fetchGameBriefsByIds");
        if (res == null || res.length == 0) return List.of();

        List<SimilarGameResponse> out = new ArrayList<>(res.length);
        for (IgdbGameBriefDto d : res) {
            if (d == null) continue;
            String coverId = (d.cover() == null) ? null : d.cover().imageId();
            out.add(new SimilarGameResponse(d.id(), d.name(), coverId));
        }
        return out;
    }

    // 인기 게임 목록 조회 (IGDB rating 기준)
    public List<IgdbPopularGameDto> getPopularGames(int limit) {
        String body = """                                                                                        
          fields id, name, cover.image_id, total_rating, total_rating_count;
          where total_rating_count > 50 & cover != null & total_rating != null;
          sort total_rating desc;
          limit %d;
          """.formatted(limit);

        IgdbPopularGameDto[] res = postReq(body, IgdbPopularGameDto[].class, GAMES_ENDPOINT, "getPopularGames");
        return res == null ? List.of() : List.of(res);
    }

    // 특정 게임의 rating 정보 조회
    public IgdbPopularGameDto getGameRating(long igdbId) {
        String body = """                                                                                        
          fields id, name, cover.image_id, total_rating, total_rating_count;
          where id = %d;
          limit 1;
          """.formatted(igdbId);

        IgdbPopularGameDto[] res = postReq(body, IgdbPopularGameDto[].class, GAMES_ENDPOINT, "getGameRating");
        return firstOrNull(res);
    }

    private <T> T postReq(String body, Class<T> responseType, String endPoint, String actionName) {
        Objects.requireNonNull(body, "IGDB request body must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        //rate limiter
        igdbRateLimiter.acquire();

        //429일 경우 재시도
        int retryCount = 0;
        while(true) {
            try {
                return igdbRestClient.post()
                        .uri(endPoint)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Client-ID", props.clientId())
                        .header("Authorization", "Bearer " + tokenService.getAccessToken())
                        .body(body)
                        .retrieve() // 요청을 보내고 응답을 가져올 준비를 하는 단계, 응답(상태코드, 헤더, 바디)을 받을 수 있는 핸들러가 만들어짐
                        .body(responseType); // 응답 body를 어떤 타입으로 변환해서 꺼낼지 정하는 것, IGDB가 JSON 배열을 준다 -> Jackson이 responseType으로 역직렬화 해줌

            } catch (RestClientResponseException e) {
                //429처리
                if (e.getStatusCode().value() == 429 && retryCount < MAX_RETRIES) {
                    retryCount++;
                    long waitMs = (long) Math.pow(2, retryCount) * 500; // 1, 2, 4 (초) ...
                    log.warn("429 발생, {}ms 후 재시도 ({}/{})", waitMs, retryCount, MAX_RETRIES);
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IgdbApiException("재시도 도중 Interrupte 발생", ex);
                    }
                    continue;
                }

                String msg = """
                        %s 실패,
                        status: %d
                        body: %s
                        """.formatted(actionName, e.getStatusCode().value(), e.getResponseBodyAsString());
                throw new IgdbApiException(msg, e);
            } catch (Exception e) {
                String msg = "%s failed. error=%s".formatted(actionName, e.getMessage());
                throw new IgdbApiException(msg, e);
            }
        }
    }

    private static <T> T firstOrNull(T[] arr) {
        if (arr == null || arr.length == 0) return null;
        return arr[0];
    }

    private String escape(String s) {
        // 검색어에 " 들어갈 수 있으니 이스케이프
        return s.replace("\"", "\\\"");
    }

    public List<IgdbGenreDto> fetchGenres() {
        String body = """
        fields id,name;
        limit 100;
        """;

        try {
            IgdbGenreDto[] res = igdbRestClient.post()
                    .uri("/genres")
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", props.clientId())
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .body(body)
                    .retrieve()
                    .body(IgdbGenreDto[].class);

            return res == null ? List.of() : List.of(res);

        } catch (RestClientResponseException e) {
            throw new IgdbApiException(
                    "IGDB fetchGenres failed: " + e.getResponseBodyAsString(),
                    e
            );
        }
    }
}
