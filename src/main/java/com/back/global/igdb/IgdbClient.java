package com.back.global.igdb;

import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.*;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.exception.IgdbApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    private static final String GAMES_ENDPOINT = "/games";
    private static final String GAME_VIDEOS_ENDPOINT = "/game_videos";

    private final RestClient igdbRestClient;
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;

    @PostConstruct
    public void warmUpToken() {
        try {
            tokenService.getAccessToken(); // 앱 시작할 때 토큰 미리 발급/캐시
        } catch (Exception e) {
            // 시작 시 실패해도 앱은 떠야 하니까 로그만 남기고 무시하는 패턴
            log.warn("Twitch token warm-up failed", e);
        }
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "searchGamesFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameDetailFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameNameFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getVideoIdFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameIdsFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameBriefByIdFallback")
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

    @CircuitBreaker(name = "igdb", fallbackMethod = "getPopularGameIdsFallback")
    public List<IgdbPopularityPrimitiveDto> getPopularGameIds(int limit) {
        String body = """
            fields game_id,value,popularity_type;
            where popularity_type = 1;
            sort value desc;
            limit %d;
          """.formatted(limit);

        IgdbPopularityPrimitiveDto[] res = postReq(body, IgdbPopularityPrimitiveDto[].class,
                "/popularity_primitives", "getPopularGameIds");
        return res == null ? List.of() : List.of(res);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGamesByIdsFallback")
    public List<IgdbPopularGameDto> getGamesByIds(List<Long> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) return List.of();

        String in = gameIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String body = """
          fields id, name, cover.image_id, total_rating, total_rating_count;
          where id = (%s);
          limit %d;
          """.formatted(in, gameIds.size());

        IgdbPopularGameDto[] res = postReq(body, IgdbPopularGameDto[].class, GAMES_ENDPOINT, "getGamesByIds");
        return res == null ? List.of() : List.of(res);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGamesByIdsFallback")
    public List<GameRow> fetchGamesByIds(List<Long> idsInOrder) {
        String idList = idsInOrder.stream().map(String::valueOf).collect(Collectors.joining(","));

        String body = """
                fields id,name,cover.image_id,genres.id,genres.name,platforms.id,platforms.name,first_release_date;
                where id = (%s);
                limit %d;
                """.formatted(idList, idsInOrder.size());

        GameRow[] games = postReq(body, GameRow[].class, GAMES_ENDPOINT, "fetchGamesByIds");

        return games == null ? List.of() : List.of(games);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameRatingFallback")
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

        try {
            return igdbRestClient.post()
                    .uri(endPoint)
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", props.clientId())
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .body(body)
                    .retrieve()
                    .body(responseType);

        } catch (RestClientResponseException e) {
            String msg = "%s 실패, status: %d, body: %s"
                    .formatted(actionName, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IgdbApiException(msg, e);
        } catch (Exception e) {
            String msg = "%s failed. error=%s".formatted(actionName, e.getMessage());
            throw new IgdbApiException(msg, e);
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

    // ── Circuit Breaker Fallback Methods ──

    private List<IgdbGameSummaryDto> searchGamesFallback(String keyword, int limit, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – searchGames fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private IgdbGameDetailDto getGameDetailFallback(long igdbId, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getGameDetail fallback, cause: {}", t.getMessage());
        return null;
    }

    private IgdbGameNameDto getGameNameFallback(long igdbId, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getGameName fallback, cause: {}", t.getMessage());
        return null;
    }

    private IgdbVideoDto getVideoIdFallback(long igdbGameId, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getVideoId fallback, cause: {}", t.getMessage());
        return null;
    }

    private List<Long> getSimilarGameIdsFallback(long igdbId, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getSimilarGameIds fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private List<SimilarGameResponse> getSimilarGameBriefByIdFallback(List<Long> ids, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getSimilarGameBriefById fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private List<IgdbPopularityPrimitiveDto> getPopularGameIdsFallback(int limit, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getPopularGameIds fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private List<IgdbPopularGameDto> getGamesByIdsFallback(List<Long> gameIds, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getGamesByIds fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private List<GameRow> fetchGamesByIdsFallback(List<Long> idsInOrder, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – fetchGamesByIds fallback, cause: {}", t.getMessage());
        return List.of();
    }

    private IgdbPopularGameDto getGameRatingFallback(long igdbId, Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – getGameRating fallback, cause: {}", t.getMessage());
        return null;
    }

    private List<IgdbGenreDto> fetchGenresFallback(Throwable t) {
        log.warn("IGDB Circuit Breaker OPEN – fetchGenres fallback, cause: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGenresFallback")
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
