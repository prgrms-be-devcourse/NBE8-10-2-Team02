package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.*;
import com.back.domain.game.game.entity.CompanyRole;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.*;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbCircuitBreakerClient;
import com.back.global.igdb.dto.IgdbVideoDto;
import com.back.global.igdb.dto.PopularGameCardDto;
import com.back.global.igdb.service.IgdbPopularRightNowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameGenreRepository gameGenreRepository;
    private final GamePlatformRepository gamePlatformRepository;
    private final GameCompanyRepository gameCompanyRepository;
    private final IgdbCircuitBreakerClient igdbClient;
    private final IgdbPopularRightNowService igdbPopularRightNowService;
    private final GameCacheService gameCacheService;

    /**
     * IGDB "Popular Right Now" 인기 게임 조회
     * - Visits, Want to Play, Twitch 시청 데이터 가중치 조합
     * - 캐시 사용 (30분)
     */
    public List<PopularGameCardDto> getIgdbPopularGames(int limit) {
        String cacheKey = "igdb_popular_" + limit;
        List<PopularGameCardDto> cached = gameCacheService.getIgdbPopularGames(cacheKey);
        if (cached != null) return cached;

        List<PopularGameCardDto> result = igdbPopularRightNowService.popularRightNow(limit);

        gameCacheService.putIgdbPopularGames(cacheKey, result);
        return result;
    }

    @Transactional(readOnly = true)
    public GameDetailResponse getGameDetail(long igdbId) {
        incrementViewCountInMemory(igdbId);

        // 1. cache
        GameDetailResponse cached = gameCacheService.getGameDetail(igdbId);
        if (cached != null) return cached;

        // 2. DB
        GameDetailResponse fromDb = gameRepository.findByIgdbId(igdbId)
                .map(this::assembleDetails)
                .orElseThrow(() -> new ServiceException("404-1", "게임을 찾을 수 없습니다. " + igdbId));

        gameCacheService.putGameDetail(igdbId, fromDb);
        return fromDb;
    }

    public GameVideoResponse getVideoId(long igdbId) {
        // 1. cache에서 찾기
        GameVideoResponse cached = gameCacheService.getVideo(igdbId);
        if (cached != null) return cached;

        // 2. api호출
        GameVideoResponse fetched = fetchVideoId(igdbId);
        gameCacheService.putVideo(igdbId, fetched);
        return fetched;
    }

    public List<SimilarGameResponse> getSimilarGames(long igdbId) {
        // 1. cachedList에서 찾기
        List<SimilarGameResponse> cachedList = gameCacheService.getSimilarList(igdbId);
        if (cachedList != null) return cachedList;

        // 2. similar ids 캐시 확인
        List<Long> ids = gameCacheService.getSimilarIds(igdbId);

        // 3. ids가 없으면 igdb에서 similarGames id만 조회 후 캐시에 저장
        if (ids == null) {
            ids = igdbClient.getSimilarGameIds(igdbId);
            if (ids == null) ids = Collections.emptyList();
            gameCacheService.putSimilarIds(igdbId, ids);
        }
        if (ids.isEmpty()) {
            gameCacheService.putSimilarList(igdbId, List.of());
            return List.of();
        }
        // 4. id들로 name + cover만 2차 조회 (limit 적용)
        List<SimilarGameResponse> list = igdbClient.getSimilarGameBriefById(ids);
        if (list == null) list = List.of();

        // 5. 결과 캐시
        gameCacheService.putSimilarList(igdbId, list);
        return list;
    }

    private GameVideoResponse fetchVideoId(long igdbId) {
        IgdbVideoDto dto = igdbClient.getVideoId(igdbId);
        if (dto == null || dto.videoId() == null){
            return GameVideoResponse.from("");
        }

        return GameVideoResponse.from(dto.videoId());
    }


    private GameDetailResponse assembleDetails(Game game) {
        List<String> genres = gameGenreRepository.findGenreNamesByGameId(game.getId());
        List<String> platforms = gamePlatformRepository.findPlatformNamesByGameId(game.getId());
        List<String> developers = gameCompanyRepository.findCompanyNamesByGameIdAndRole(game.getId(), CompanyRole.DEVELOPER);
        List<String> publishers = gameCompanyRepository.findCompanyNamesByGameIdAndRole(game.getId(), CompanyRole.PUBLISHER);

        return GameDetailResponse.from(game, genres, platforms, developers, publishers);
    }

    private void incrementViewCountInMemory(long igdbId) {
        gameCacheService.incrementViewCount(igdbId);
    }

    // 5분마다 캐시에 있는 조회수를 DB에 반영
    @Scheduled(fixedRate = 300000)  // 5분
    @Transactional
    public void flushViewCountsToDb() {
        Map<Long, AtomicLong> snapshot = gameCacheService.getViewCountSnapshot();

        if (snapshot.isEmpty()) return;

        log.info("조회수 DB 반영 시작: {} 건", snapshot.size());

        snapshot.forEach((igdbId, counter) -> {
            long delta = counter.getAndSet(0);  // 가져오고 0으로 리셋
            if (delta > 0) {
                try {
                    gameRepository.incrementViewCount(igdbId, delta);
                } catch (Exception e) {
                    log.warn("조회수 반영 실패: igdbId={}, delta={}", igdbId, delta, e);
                    // 실패한 건 다시 더해줌
                    counter.addAndGet(delta);
                }
            }
        });

        log.info("조회수 DB 반영 완료");
    }

    // 서버 종료 시 flush (데이터 유실 방지)
/*    @PreDestroy
    public void onShutdown() {
        log.info("서버 종료 - 조회수 flush");
        flushViewCountsToDb();
    }*/

    public Optional<Game> findById(int id) {
        return gameRepository.findById(id);
    }

    public Game createGame(Long igdbId, String name, String summary, String coverImage, LocalDate firstReleaseDate) {
        Game game = Game.createGame(
                        igdbId,
                        name,
                        summary,
                        coverImage,
                        firstReleaseDate
                );        return gameRepository.save(game);
    }
}