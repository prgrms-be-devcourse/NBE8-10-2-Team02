package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.*;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.*;
import com.back.global.igdb.service.IgdbPopularRightNowService;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final GameGenreRepository gameGenreRepository;
    private final GamePlatformRepository gamePlatformRepository;
    private final IgdbClient igdbClient;
    private final IgdbPopularRightNowService igdbPopularRightNowService;

    private final Cache<Long, GameDetailResponse> gameDetailCache;
    private final Cache<Long, GameVideoResponse> videoIdCache;
    private final Cache<Long, List<Long>> similarIdsCache;
    private final Cache<Long, List<SimilarGameResponse>> similarListCache;
    private final Cache<String, List<PopularGameResponse>> popularGamesCache;
    private final Cache<String, List<PopularGameCardDto>> igdbPopularGamesCache;
    private final Cache<Long, AtomicLong> viewCountCache;

    private static final Duration DB_STALE_AFTER = Duration.ofDays(7);

    public List<GameSearchByNameResponse> search(String q) {
        // 1. cache에서 찾기

        // 2. api호출
        return igdbClient.searchGames(q, 10).stream()
                .map(GameSearchByNameResponse::fromDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PopularGameResponse> getPopularGames(int limit) {
        // 1. 캐시 확인
        String cacheKey = "popular_" + limit;
        List<PopularGameResponse> cached = popularGamesCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        // 2. DB에서 조회
        List<Game> games = gameRepository.findPopularGames(PageRequest.of(0, limit));

        List<PopularGameResponse> result = games.stream()
                .map(PopularGameResponse::fromGame)
                .toList();

        popularGamesCache.put(cacheKey, result);
        return result;
    }

    /**
     * IGDB "Popular Right Now" 인기 게임 조회
     * - Visits, Want to Play, Twitch 시청 데이터 가중치 조합
     * - 캐시 사용 (30분)
     */
    public List<PopularGameCardDto> getIgdbPopularGames(int limit) {
        String cacheKey = "igdb_popular_" + limit;
        List<PopularGameCardDto> cached = igdbPopularGamesCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        List<PopularGameCardDto> result = igdbPopularRightNowService.popularRightNow(limit);

        igdbPopularGamesCache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public List<PopularGameResponse> getPopularGamesHybrid(int limit) {
        // 1. 캐시 확인
        String cacheKey = "popular_hybrid_" + limit;
        List<PopularGameResponse> cached = popularGamesCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        // 2. popularity_primitives에서 인기 game_id + value 조회
        List<IgdbPopularityPrimitiveDto> primitives = igdbClient.getPopularGameIds(limit);
        if (primitives.isEmpty()) return List.of();

        // 3. value 정규화 (최대값을 100으로)
        double maxValue = primitives.stream()
                .mapToDouble(IgdbPopularityPrimitiveDto::value)
                .max().orElse(1.0);

        Map<Long, Double> normalizedScores = primitives.stream()
                .collect(Collectors.toMap(
                        IgdbPopularityPrimitiveDto::gameId,
                        p -> (p.value() / maxValue) * 100.0,
                        (a, b) -> Math.max(a, b) // 같은 gameId가 여러 타입으로 올 수 있음
                ));

        // 4. game_id들로 IGDB에서 게임 정보(name, cover) 조회
        List<Long> gameIds = new ArrayList<>(normalizedScores.keySet());
        List<IgdbPopularGameDto> igdbGames = igdbClient.getGamesByIds(gameIds);

        Map<Long, IgdbPopularGameDto> igdbGameMap = igdbGames.stream()
                .collect(Collectors.toMap(IgdbPopularGameDto::id, Function.identity()));

        // 5. DB에 있는 게임은 자체 데이터 포함해서 반영
        List<PopularGameResponse> result = primitives.stream()
                .map(p -> {
                    long gameId = p.gameId();
                    double normalizedScore = normalizedScores.getOrDefault(gameId, 0.0);
                    IgdbPopularGameDto igdbDto = igdbGameMap.get(gameId);

                    if (igdbDto == null) return null; // 게임 정보를 못 가져온 경우

                    return gameRepository.findByIgdbId(gameId)
                            .map(game -> PopularGameResponse.fromGame(game, normalizedScore))
                            .orElseGet(() -> PopularGameResponse.fromIgdb(igdbDto, normalizedScore));
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.popularityScore(), a.popularityScore()))
                .toList();

        popularGamesCache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public GameDetailResponse getGameDetail(long igdbId) {
        // 1. cache에서 찾기
        incrementViewCountInMemory(igdbId); //조회수 증가
        GameDetailResponse cached = gameDetailCache.getIfPresent(igdbId);
        if (cached != null) return cached;

        // 2. cache miss -> DB에서 찾기
        GameDetailResponse fromDb = findDetailFromDb(igdbId);
        if (fromDb != null) {
            gameDetailCache.put(igdbId, fromDb);
            return fromDb;
        }

        // 3. DB miss or stale -> api호출
        GameDetailResponse fetched = fetchPersistAndAssemble(igdbId);
        gameDetailCache.put(igdbId, fetched);
        return fetched;
    }

    public GameVideoResponse getVideoId(long igdbId) {
        // 1. cache에서 찾기
        GameVideoResponse cached = videoIdCache.getIfPresent(igdbId);
        if (cached != null) return cached;

        // 2. api호출
        GameVideoResponse fetched = fetchVideoId(igdbId);
        videoIdCache.put(igdbId, fetched);
        return fetched;
    }

    public List<SimilarGameResponse> getSimilarGames(long igdbId) {
        // 1. cachedList에서 찾기
        List<SimilarGameResponse> cachedList = similarListCache.getIfPresent(igdbId);
        if (cachedList != null) return cachedList;

        // 2. similar ids 캐시 확인
        List<Long> ids = similarIdsCache.getIfPresent(igdbId);

        // 3. ids가 없으면 igdb에서 similarGames id만 조회 후 캐시에 저장
        if (ids == null) {
            ids = igdbClient.getSimilarGameIds(igdbId);
            if (ids == null) ids = Collections.emptyList();
            similarIdsCache.put(igdbId, ids);
        }
        if (ids.isEmpty()) {
            similarListCache.put(igdbId, List.of());
            return List.of();
        }
        // 4. id들로 name + cover만 2차 조회 (limit 적용)
        List<SimilarGameResponse> list = igdbClient.getSimilarGameBriefById(ids);
        if (list == null) list = List.of();

        // 5. 결과 캐시
        similarListCache.put(igdbId, list);
        return list;
    }

    private GameVideoResponse fetchVideoId(long igdbId) {
        IgdbVideoDto dto = igdbClient.getVideoId(igdbId);
        if (dto == null || dto.videoId() == null){
            return GameVideoResponse.from("");
        }

        return GameVideoResponse.from(dto.videoId());
    }


    private GameDetailResponse findDetailFromDb(long igdbId) {
        return gameRepository.findByIgdbId(igdbId)
                .filter(this::isFresh) // stale이면 null 반환, API 재호출
                .map(this::assembleDetails)
                .orElse(null);
    }

    private GameDetailResponse assembleDetails(Game game) {
        List<String> genres = gameGenreRepository.findGenreNamesByGameId(game.getId());
        List<String> platforms = gamePlatformRepository.findPlatformNamesByGameId(game.getId());
        return GameDetailResponse.from(game, genres, platforms);
    }

    private GameDetailResponse fetchPersistAndAssemble(long igdbId) {
        //igdb 호출
        IgdbGameDetailDto dto = igdbClient.getGameDetail(igdbId);
        if (dto == null) {
            throw new ServiceException("404-1", "게임을 찾을 수 없습니다. " + igdbId);
        }

        //game upsert
        Game game = gameRepository.findByIgdbId(igdbId)
                .orElseGet(() -> {

                    CompanyNames companyNames = extractCompanyNames(dto.involvedCompanies());
                    return Game.createGame(
                            dto.id(),
                            dto.name(),
                            dto.summary() != null ? dto.summary() : "",
                            companyNames.developers,
                            companyNames.publishers,
                            dto.cover() != null ? dto.cover().imageId() : null,
                            dto.firstReleaseDateEpochSeconds());
                    }
                );
        String coverImageId = dto.cover() == null ? null : dto.cover().imageId();
        String summary = dto.summary() == null ? "" : dto.summary();
        CompanyNames companyNames = extractCompanyNames(dto.involvedCompanies());

        game.updateDetail(dto.name(), summary, coverImageId, dto.firstReleaseDateEpochSeconds());
        game.updateCompanies(companyNames.developers(), companyNames.publishers());
        game = gameRepository.save(game);

        upsertAndLinkGenre(game, dto.genres());
        upsertAndLinkPlatform(game, dto.platforms());

        GameDetailResponse assembled = assembleDetails(game);
        return assembled;
    }

    private record CompanyNames(List<String> developers, List<String> publishers) {
    }
    @NotNull
    private static CompanyNames extractCompanyNames(List<IgdbInvolvedCompanyDto> companies) {
        if (companies == null) return new CompanyNames(List.of(), List.of());

        List<String> developers = new ArrayList<>();
        List<String> publishers = new ArrayList<>();

        for (IgdbInvolvedCompanyDto ic : companies) {
            if (ic == null || ic.company() == null) continue;
            if (ic.developer()) developers.add(ic.company().name());
            if (ic.publisher()) publishers.add(ic.company().name());
        }
        return new CompanyNames(developers, publishers);
    }

    private void upsertAndLinkGenre(Game game, List<IgdbGenreDto> igdbGenres) {
        if (igdbGenres == null || igdbGenres.isEmpty()) return;

        List<Long> igdbIds = igdbGenres.stream()
                .map(IgdbGenreDto::id)
                .distinct()
                .toList();
        //id로 한번에 조회
        List<Genre> existing = genreRepository.findByIgdbIdIn(igdbIds);
        //map으로 변환
        Map<Long, Genre> byIgdbId = existing.stream().collect(Collectors.toMap(Genre::getIgdbId, g -> g));
        //없는거만 생성
        List<Genre> toCreate = igdbGenres.stream()
                .filter(g -> !byIgdbId.containsKey(g.id()))
                .map(g -> Genre.createGenre(g.id(), g.name()))
                .toList();

        if (!toCreate.isEmpty()) {
            //없는거만 bulk insert
            List<Genre> created = genreRepository.saveAll(toCreate);
            for (Genre g : created) {
                byIgdbId.put(g.getIgdbId(), g);
            }
        }

        //링크
        //중복 방지 정책이 필요함(동시성이슈) exists or unique constraint
        for (Long gid : igdbIds) {
            Genre genre = byIgdbId.get(gid);
            gameGenreRepository.insertIgnore(game.getId(), genre.getId());
        }
    }

    private void upsertAndLinkPlatform(Game game, List<IgdbPlatformDto> igdbPlatforms) {
        if (igdbPlatforms == null || igdbPlatforms.isEmpty()) return;

        List<Long> igdbIds = igdbPlatforms.stream()
                .map(IgdbPlatformDto::id)
                .distinct()
                .toList();

        List<Platform> existing = platformRepository.findByIgdbIdIn(igdbIds);

        Map<Long, Platform> byIgdbId = existing.stream()
                .collect(Collectors.toMap(Platform::getIgdbId, Function.identity()));

        List<Platform> toCreate = igdbPlatforms.stream()
                .filter(p -> !byIgdbId.containsKey(p.id()))
                .map(p -> Platform.createPlatform(p.id(), p.name()))
                .toList();

        if (!toCreate.isEmpty()) {
            List<Platform> created = platformRepository.saveAll(toCreate);
            for (Platform p : created) byIgdbId.put(p.getIgdbId(), p);
        }

        for (Long pid : igdbIds) {
            Platform platform = byIgdbId.get(pid);
            gamePlatformRepository.insertIgnore(game.getId(), platform.getId());
        }
    }

    private boolean isFresh(Game game) {
        Instant t = game.getLastFetchedAt();
        return t != null && t.isAfter(Instant.now().minus(DB_STALE_AFTER));
    }

    private void incrementViewCountInMemory(long igdbId) {
        viewCountCache.get(igdbId, k -> new AtomicLong(0)).incrementAndGet();
    }

    // 5분마다 캐시에 있는 조회수를 DB에 반영
    @Scheduled(fixedRate = 300000)  // 5분
    @Transactional
    public void flushViewCountsToDb() {
        Map<Long, AtomicLong> snapshot = new HashMap<>(viewCountCache.asMap());

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