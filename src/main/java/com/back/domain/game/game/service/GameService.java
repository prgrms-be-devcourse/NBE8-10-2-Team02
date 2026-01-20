package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final IgdbClient igdbClient;

    //DB stale 판단 기준 -> snapshot 후 7일이 지나면 stale
    private static final Duration DB_STALE_AFTER = Duration.ofDays(7);

    public List<GameSearchResponse> search(String q) {
        // 1. cache에서 찾기
        // 2. api호출
        return igdbClient.searchGames(q, 10).stream()
                .map(GameSearchResponse::fromDto).toList();
    }

    @Transactional
    public GameDetailResponse getGameDetail(long igdbId) {
        // 1. cache에서 찾기

        // 2. cache miss -> DB에서 찾기
        Optional<Game> og = gameRepository.findByIgdbId(igdbId);
        if (og.isPresent() && isFresh(og.get())) {
            GameDetailResponse res = GameDetailResponse.from(og.get()); // 필요하면 join fetch로 개선
            // 캐시에 넣고 gameDetailCache.put(igdbId, res);
            return res;
        }
        // 3. DB miss or stale -> api호출
        GameDetailResponse res = fetchAndUpsertFromIgdb(igdbId);
        //gameDetailCache.put(igdbId, res);
        return res;
    }

    @Transactional
    private GameDetailResponse fetchAndUpsertFromIgdb(long igdbId) {
        IgdbGameDetailDto dto = igdbClient.getGameDetail(igdbId);
        if (dto == null)
            return null;

        Game game = gameRepository.findByIgdbId(igdbId)
                .orElseGet(() -> Game.createGame(
                        dto.id(),
                        dto.name(),
                        dto.summary(),
                        dto.cover().imageId(),
                        dto.firstReleaseDateEpochSeconds()));
        game = gameRepository.save(game);

        //upsertGenres(dto.genres());
    }

    /*private Map<Long, Genre> upsertGenres(List<IgdbGenreDto> genres) {
        if (genres == null || genres.isEmpty()) return Map.of();

        //id
        List<Long> ids = genres.stream().map(IgdbGenreDto::id).toList();
        genreRepository.findByIgdbIn(ids);

    }*/

    private boolean isFresh(Game game) {
        Instant t = game.getLastFetchedAt();
        return t != null && t.isAfter(Instant.now().minus(DB_STALE_AFTER));
    }
}