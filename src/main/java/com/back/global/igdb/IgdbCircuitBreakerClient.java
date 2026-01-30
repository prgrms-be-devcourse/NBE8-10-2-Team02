package com.back.global.igdb;

import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IgdbClient를 감싸는 Circuit Breaker 래퍼.
 * IGDB 장애 시 빠른 실패 처리(fallback)를 담당한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbCircuitBreakerClient {

    private final IgdbClient igdbClient;

    @CircuitBreaker(name = "igdb", fallbackMethod = "searchGamesFallback")
    public List<IgdbGameSummaryDto> searchGames(String keyword, int limit) {
        return igdbClient.searchGames(keyword, limit);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameDetailFallback")
    public IgdbGameDetailDto getGameDetail(long igdbId) {
        return igdbClient.getGameDetail(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameNameFallback")
    public IgdbGameNameDto getGameName(long igdbId) {
        return igdbClient.getGameName(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getVideoIdFallback")
    public IgdbVideoDto getVideoId(long igdbGameId) {
        return igdbClient.getVideoId(igdbGameId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameIdsFallback")
    public List<Long> getSimilarGameIds(long igdbId) {
        return igdbClient.getSimilarGameIds(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameBriefByIdFallback")
    public List<SimilarGameResponse> getSimilarGameBriefById(List<Long> ids) {
        return igdbClient.getSimilarGameBriefById(ids);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getPopularGameIdsFallback")
    public List<IgdbPopularityPrimitiveDto> getPopularGameIds(int limit) {
        return igdbClient.getPopularGameIds(limit);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGamesByIdsFallback")
    public List<IgdbPopularGameDto> getGamesByIds(List<Long> gameIds) {
        return igdbClient.getGamesByIds(gameIds);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGamesByIdsFallback")
    public List<GameRow> fetchGamesByIds(List<Long> idsInOrder) {
        return igdbClient.fetchGamesByIds(idsInOrder);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameRatingFallback")
    public IgdbPopularGameDto getGameRating(long igdbId) {
        return igdbClient.getGameRating(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGenresFallback")
    public List<IgdbGenreDto> fetchGenres() {
        return igdbClient.fetchGenres();
    }

    // ── Fallback Methods ──

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
}
