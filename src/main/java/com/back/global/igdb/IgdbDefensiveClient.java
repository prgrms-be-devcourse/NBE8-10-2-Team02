package com.back.global.igdb;

import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.global.igdb.dto.*;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service → IgdbDefensiveClient → IgdbClient → IGDB API
 *           (서킷브레이커 + Bulkhead + RateLimiter + fallback)    (순수 HTTP)
 *
 * 방어 계층:
 *  - RateLimiter: 슬롯 없으면 200ms 후 즉시 DB fallback (사용자 줄세우기 방지)
 *  - Bulkhead: 동시 IGDB 호출을 최대 10개로 제한 → 나머지는 즉시 DB fallback
 *  - CircuitBreaker: 실패율 40% 초과 시 30초 OPEN → 즉시 DB fallback
 *  - Bulkhead 거부(BulkheadFullException)는 CB 실패로 집계하지 않음 (ignore-exceptions 설정)
 *
 * Aspect 실행 순서 (CB outer → Bulkhead → RateLimiter inner → IgdbClient):
 *  CB OPEN  → 즉시 fallback (Bulkhead, RateLimiter 진입 없음)
 *  CB CLOSED, Bulkhead FULL → BulkheadFullException → CB가 ignore → fallback 호출
 *  CB CLOSED, Bulkhead OK, RateLimiter 거부 → RateLimiter fallback 즉시 호출 (CB 미집계)
 *  CB CLOSED, Bulkhead OK, RateLimiter OK   → 실제 IGDB 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbDefensiveClient {

    private final IgdbClient igdbClient;
    private final GameRepository gameRepository;

    @CircuitBreaker(name = "igdb", fallbackMethod = "searchGamesFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "searchGamesFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "searchGamesFallback")
    public List<IgdbGameSummaryDto> searchGames(String keyword, int limit) {
        return igdbClient.searchGames(keyword, limit);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameNameFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getGameNameFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getGameNameFallback")
    public IgdbGameNameDto getGameName(long igdbId) {
        return igdbClient.getGameName(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getVideoIdFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getVideoIdFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getVideoIdFallback")
    public IgdbVideoDto getVideoId(long igdbGameId) {
        return igdbClient.getVideoId(igdbGameId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameIdsFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getSimilarGameIdsFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getSimilarGameIdsFallback")
    public List<Long> getSimilarGameIds(long igdbId) {
        return igdbClient.getSimilarGameIds(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getSimilarGameBriefByIdFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getSimilarGameBriefByIdFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getSimilarGameBriefByIdFallback")
    public List<SimilarGameResponse> getSimilarGameBriefById(List<Long> ids) {
        return igdbClient.getSimilarGameBriefById(ids);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getPopularGameIdsFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getPopularGameIdsFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getPopularGameIdsFallback")
    public List<IgdbPopularityPrimitiveDto> getPopularGameIds(int limit) {
        return igdbClient.getPopularGameIds(limit);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGamesByIdsFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getGamesByIdsFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getGamesByIdsFallback")
    public List<IgdbPopularGameDto> getGamesByIds(List<Long> gameIds) {
        return igdbClient.getGamesByIds(gameIds);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGamesByIdsFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "fetchGamesByIdsFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "fetchGamesByIdsFallback")
    public List<GameRow> fetchGamesByIds(List<Long> idsInOrder) {
        return igdbClient.fetchGamesByIds(idsInOrder);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "getGameRatingFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "getGameRatingFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "getGameRatingFallback")
    public IgdbPopularGameDto getGameRating(long igdbId) {
        return igdbClient.getGameRating(igdbId);
    }

    @CircuitBreaker(name = "igdb", fallbackMethod = "fetchGenresFallback")
    @Bulkhead(name = "igdb", fallbackMethod = "fetchGenresFallback")
    @RateLimiter(name = "igdb", fallbackMethod = "fetchGenresFallback")
    public List<IgdbGenreDto> fetchGenres() {
        return igdbClient.fetchGenres();
    }

    // ── Fallback Methods ──
    // IGDB 장애, Bulkhead 초과, RateLimiter 거부 시 로컬 DB 데이터로 대체 응답

    private List<IgdbGameSummaryDto> searchGamesFallback(String keyword, int limit, Throwable t) {
        log.warn("IGDB fallback – searchGames(keyword={}), cause: {}", keyword, t.getMessage());
        return gameRepository.findByNameContainingIgnoreCaseLimited(keyword, PageRequest.of(0, limit))
                .stream()
                .map(g -> new IgdbGameSummaryDto(
                        g.getIgdbId(),
                        g.getName(),
                        g.getSummary(),
                        toEpochSeconds(g),
                        toCoverDto(g),
                        List.of(),
                        List.of()
                ))
                .toList();
    }

    private IgdbGameNameDto getGameNameFallback(long igdbId, Throwable t) {
        log.warn("IGDB fallback – getGameName(igdbId={}), cause: {}", igdbId, t.getMessage());
        return gameRepository.findByIgdbId(igdbId)
                .map(g -> new IgdbGameNameDto(g.getIgdbId(), g.getName()))
                .orElse(null);
    }

    private IgdbVideoDto getVideoIdFallback(long igdbGameId, Throwable t) {
        log.warn("IGDB fallback – getVideoId(igdbId={}), cause: {}", igdbGameId, t.getMessage());
        return null;  // 비디오 ID는 로컬 DB에 없으므로 null 유지 (호출부에서 빈 문자열 처리)
    }

    private List<Long> getSimilarGameIdsFallback(long igdbId, Throwable t) {
        log.warn("IGDB fallback – getSimilarGameIds(igdbId={}), cause: {}", igdbId, t.getMessage());
        return List.of();  // GameService.getSimilarGames()는 이미 pgvector를 직접 사용하므로 영향 없음
    }

    private List<SimilarGameResponse> getSimilarGameBriefByIdFallback(List<Long> ids, Throwable t) {
        log.warn("IGDB fallback – getSimilarGameBriefById, cause: {}", t.getMessage());
        return gameRepository.findByIgdbIdIn(ids).stream()
                .map(g -> new SimilarGameResponse(g.getIgdbId(), g.getName(), g.getCoverImageId()))
                .toList();
    }

    private List<IgdbPopularityPrimitiveDto> getPopularGameIdsFallback(int limit, Throwable t) {
        log.warn("IGDB fallback – getPopularGameIds(limit={}), cause: {}", limit, t.getMessage());
        return gameRepository.findByOrderByLikeCountDesc(PageRequest.of(0, limit))
                .stream()
                .map(g -> new IgdbPopularityPrimitiveDto(0L, g.getIgdbId(), g.getLikeCount(), 1))
                .toList();
    }

    private List<IgdbPopularGameDto> getGamesByIdsFallback(List<Long> gameIds, Throwable t) {
        log.warn("IGDB fallback – getGamesByIds, cause: {}", t.getMessage());
        return gameRepository.findByIgdbIdIn(gameIds).stream()
                .map(g -> new IgdbPopularGameDto(
                        g.getIgdbId(),
                        g.getName(),
                        toCoverDto(g),
                        g.getAggregatedRating(),
                        null
                ))
                .toList();
    }

    private List<GameRow> fetchGamesByIdsFallback(List<Long> idsInOrder, Throwable t) {
        log.warn("IGDB fallback – fetchGamesByIds, cause: {}", t.getMessage());
        Map<Long, Game> byIgdbId = gameRepository.findByIgdbIdIn(idsInOrder).stream()
                .collect(Collectors.toMap(Game::getIgdbId, g -> g));
        return idsInOrder.stream()
                .map(byIgdbId::get)
                .filter(Objects::nonNull)
                .map(g -> new GameRow(
                        g.getIgdbId(),
                        g.getName(),
                        toCoverDto(g),
                        List.of()
                ))
                .toList();
    }

    private IgdbPopularGameDto getGameRatingFallback(long igdbId, Throwable t) {
        log.warn("IGDB fallback – getGameRating(igdbId={}), cause: {}", igdbId, t.getMessage());
        return gameRepository.findByIgdbId(igdbId)
                .map(g -> new IgdbPopularGameDto(
                        g.getIgdbId(),
                        g.getName(),
                        toCoverDto(g),
                        g.getAggregatedRating(),
                        null
                ))
                .orElse(null);
    }

    private List<IgdbGenreDto> fetchGenresFallback(Throwable t) {
        log.warn("IGDB fallback – fetchGenres, cause: {}", t.getMessage());
        return List.of();
    }

    // ── Helper Methods ──

    private IgdbCoverDto toCoverDto(Game g) {
        return g.getCoverImageId() != null ? new IgdbCoverDto(0L, g.getCoverImageId()) : null;
    }

    private Long toEpochSeconds(Game g) {
        return g.getFirstReleaseDate() != null
                ? g.getFirstReleaseDate().atStartOfDay(ZoneOffset.UTC).toEpochSecond()
                : null;
    }
}
