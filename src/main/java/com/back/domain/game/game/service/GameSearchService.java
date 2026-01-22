package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameSearchCondition;

import com.back.domain.game.game.dto.GameSearchResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
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


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameSearchService {

    private final GameSearchRepository gameSearchRepository;
    private final IgdbService igdbService;
    private final GenreRepository genreRepository;


    public List<GameSearchResponse> search(GameSearchCondition condition) {
        /* 플랫폼 코드 → IGDB 플랫폼 ID 확장 */
        if (condition.getPlatformCode() != null) {
            condition.setPlatformIgdbIds(
                    PLATFORM_MAP.get(condition.getPlatformCode())
            );
        }

        /* DB 검색 */
        List<Game> games = gameSearchRepository.searchByCondition(condition);

        if (!games.isEmpty()) {
            return games.stream()
                    .map(GameSearchResponse::fromEntity)
                    .toList();
        }

        /* DB에 없을 때만 IGDB 검색 */
        List<IgdbGameSummaryDto> igdbGames = igdbService.search(condition);

        /* IGDB 장르 ID 수집 */
        Set<Long> genreIgdbIds = igdbGames.stream()
                .flatMap(g -> g.genres().stream())
                .collect(Collectors.toSet());

        /* DB 장르 매핑 */
        Map<Long, String> genreMap =
                genreRepository.findByIgdbIdIn(genreIgdbIds).stream()
                        .collect(Collectors.toMap(
                                Genre::getIgdbId,
                                Genre::getName
                        ));

        /* IGDB → 응답 변환 */
        return igdbGames.stream()
                .map(d -> GameSearchResponse.fromDto(d, genreMap))
                .toList();
    }

}
