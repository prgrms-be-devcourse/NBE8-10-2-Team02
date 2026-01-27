package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameSearchCondition;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameSearchRepository;
import com.back.domain.game.game.repository.GameSearchRepositoryTest;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.global.igdb.service.IgdbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.back.domain.game.platform.PlatformGroup.PLATFORM_MAP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GameSearchServiceTest {

    @InjectMocks
    private GameSearchService gameSearchService;

    @Mock
    private GameSearchRepository gameSearchRepository;

    @Mock
    private IgdbService igdbService;

    @Mock
    private GenreRepository genreRepository;

    @Test
    void 플랫폼_코드가_NINTENDO면_IGDB_플랫폼_ID로_확장된다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");
        condition.setPlatformCode("NINTENDO");

        // DB에 결과 없다고 가정
        when(gameSearchRepository.searchByCondition(any()))
                .thenReturn(List.of());

        // IGDB fallback 막기용
        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        // when
        gameSearchService.search(condition);

        // then
        ArgumentCaptor<GameSearchCondition> captor =
                ArgumentCaptor.forClass(GameSearchCondition.class);

        verify(gameSearchRepository).searchByCondition(captor.capture());

        GameSearchCondition captured = captor.getValue();

        assertThat(captured.getPlatformIgdbIds())
                .containsExactlyInAnyOrderElementsOf(
                        PLATFORM_MAP.get("NINTENDO")
                );

    }

    @Test
    void DB에_검색_결과가_있으면_IGDB를_호출하지_않는다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");

        Game game = Game.builder()
                .igdbId(1L)
                .name("Zelda")
                .build();

        when(gameSearchRepository.searchByCondition(any()))
                .thenReturn(List.of(game));

        // when
        gameSearchService.search(condition);

        // then
        verify(igdbService, org.mockito.Mockito.never()).search(any());
    }

    @Test
    void DB에_검색_결과가_없으면_IGDB를_호출한다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");

        when(gameSearchRepository.searchByCondition(any()))
                .thenReturn(List.of());

        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        // when
        gameSearchService.search(condition);

        // then
        verify(igdbService).search(any());
    }


}
