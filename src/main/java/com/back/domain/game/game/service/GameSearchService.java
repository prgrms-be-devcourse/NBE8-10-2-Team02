package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameSearchCondition;

import com.back.domain.game.game.dto.GameSearchResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.repository.GameRepository;
import com.back.domain.game.game.repository.GameSearchRepository;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.service.IgdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.back.domain.game.platform.PlatformGroup.PLATFORM_MAP;
import static com.back.global.search.SearchNormalizer.normalize;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameSearchService {

    private final IgdbService igdbService;
    private final GenreRepository genreRepository;
    private final GameSearchRepository gameSearchRepository;


    public List<GameSearchResponse> search(GameSearchCondition condition) {

        condition.setQuery(normalize(condition.getQuery()));

        boolean hasPlatformFilter =
                condition.getPlatformCode() != null &&
                        !condition.getPlatformCode().isBlank();

        if (hasPlatformFilter) {
            String normalized = condition.getPlatformCode().trim().toUpperCase();
            List<Long> mappedIds = PLATFORM_MAP.get(normalized);
            condition.setPlatformIgdbIds(mappedIds);
        } else {
            condition.setPlatformIgdbIds(null);
        }

        // 1. IGDB 검색
        List<Game> games =
                gameSearchRepository.searchByCondition(condition);

        if (!games.isEmpty()) {
            return games.stream()
                    .map(GameSearchResponse::from)
                    .toList();
        }

//        DB에 없으면 IGDB 검색
        List<IgdbGameSummaryDto> igdbGames = igdbService.search(condition);

        // 2. 장르 매핑
        Set<Long> genreIgdbIds = igdbGames.stream()
                .filter(g -> g.genres() != null)
                .flatMap(g -> g.genres().stream())
                .collect(Collectors.toSet());

        Map<Long, String> genreMap =
                genreRepository.findByIgdbIdIn(genreIgdbIds).stream()
                        .collect(Collectors.toMap(
                                Genre::getIgdbId,
                                Genre::getName
                        ));


//        플랫폼 수집
        Set<Long> platformIds = igdbGames.stream()
                .filter(g -> g.platforms() != null)
                .flatMap(g -> g.platforms().stream())
                .collect(Collectors.toSet());

        Map<Long, String> platformMap =
                igdbService.getPlatformNameMap(platformIds);



        // 4. DTO 변환
        return igdbGames.stream()
                .map(d -> GameSearchResponse.fromDto(d, genreMap, platformMap))
                .toList();

    }


}
