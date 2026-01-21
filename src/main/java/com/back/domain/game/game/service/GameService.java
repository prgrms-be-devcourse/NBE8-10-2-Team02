package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchByNameResponse;
import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.dto.IgdbPlatformDto;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final GameGenreRepository gameGenreRepository;
    private final GamePlatformRepository gamePlatformRepository;
    private final IgdbClient igdbClient;

    private final Cache<Long, GameDetailResponse> gameDetailCache;

    //DB stale 판단 기준 -> snapshot 후 7일이 지나면 stale
    private static final Duration DB_STALE_AFTER = Duration.ofDays(7);

    public List<GameSearchByNameResponse> search(String q) {
        // 1. cache에서 찾기

        // 2. api호출
        return igdbClient.searchGames(q, 10).stream()
                .map(GameSearchByNameResponse::fromDto).toList();
    }

    @Transactional(readOnly = true)
    public GameDetailResponse getGameDetail(long igdbId) {
        // 1. cache에서 찾기
        GameDetailResponse cached = gameDetailCache.getIfPresent(igdbId);
        if (cached != null)
            return cached;

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

    private GameDetailResponse findDetailFromDb(long igdbId) {
        return gameRepository.findByIgdbId(igdbId)
                .map(game -> {
                    List<String> genres = gameGenreRepository.findGenreNamesByGameId(game.getId());
                    List<String> platforms = gamePlatformRepository.findPlatformNamesByGameId(game.getId());
                    return new GameDetailResponse(
                            game.getIgdbId(),
                            game.getName(),
                            game.getSummary(),
                            game.getFirstReleaseDate(),
                            game.getCoverImageId(),
                            genres,
                            platforms
                    );
                })
                .orElse(null);
    }

    @Transactional
    private GameDetailResponse fetchPersistAndAssemble(long igdbId) {
        //igdb 호출
        IgdbGameDetailDto dto = igdbClient.getGameDetail(igdbId);
        if (dto == null) return null;

        //game upsert
        Game game = gameRepository.findByIgdbId(igdbId)
                .orElseGet(() -> Game.createGame(
                        dto.id(),
                        dto.name(),
                        dto.summary() != null ? dto.summary() : "",
                        dto.cover() != null ? dto.cover().imageId() : null,
                        dto.firstReleaseDateEpochSeconds() != null ? dto.firstReleaseDateEpochSeconds() : 0L)
                );
        //TODO: null check
        game.updateDetail(dto.name(), dto.summary(), dto.cover().imageId(), dto.firstReleaseDateEpochSeconds());
        game = gameRepository.save(game);

        upsertAndLinkGenre(game, dto.genres());
        upsertAndLinkPlatform(game, dto.platforms());

        GameDetailResponse assembled = findDetailFromDb(game.getIgdbId());
        if (assembled == null) {
            //TODO: error code 수정
            throw new ServiceException("500-1","failed to assemble game detail for igdbId=" + igdbId);
        }
        return assembled;
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
}