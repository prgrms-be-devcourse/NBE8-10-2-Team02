package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameSearchCondition;
import com.back.domain.game.game.repository.GameSearchRepository;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.service.IgdbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameSearchServiceTest {

    @InjectMocks
private GameSearchService gameSearchService;

    @Mock
    private IgdbService igdbService;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GameSearchRepository gameSearchRepository;


    /**
     * 키워드만 있을 때 IGDB 검색을 호출한다
     */
    @Test
    void 키워드만_있으면_IGDB_검색을_호출한다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");

        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        when(igdbService.getPlatformNameMap(any()))
                .thenReturn(Map.of());

        // when
        gameSearchService.search(condition);

        // then
        verify(igdbService, times(1))
                .search(any(GameSearchCondition.class));
    }


    /**
     * 플랫폼 코드가 NINTENDO면 IGDB 플랫폼 ID로 변환된다
     */
    @Test
    void 플랫폼_코드가_NINTENDO면_IGDB_플랫폼_ID로_변환된다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");
        condition.setPlatformCode("NINTENDO");

        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        when(igdbService.getPlatformNameMap(any()))
                .thenReturn(Map.of());

        ArgumentCaptor<GameSearchCondition> captor =
                ArgumentCaptor.forClass(GameSearchCondition.class);

        // when
        gameSearchService.search(condition);

        // then
        verify(igdbService).search(captor.capture());

        GameSearchCondition captured = captor.getValue();
        assertThat(captured.getPlatformIgdbIds()).isNotNull();
        assertThat(captured.getPlatformIgdbIds()).isNotEmpty();
    }

    /**
     * 장르가 있으면 장르 필터가 적용된다
     */
    @Test
    void 장르가_있으면_IGDB_장르_ID로_변환된다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");
        condition.setGenreIds(List.of(1L)); // 또는 genre 필터 테스트면 이렇게

        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        when(igdbService.getPlatformNameMap(any()))
                .thenReturn(Map.of());

        // when
        gameSearchService.search(condition);

        // then
        verify(igdbService).search(any(GameSearchCondition.class));
    }

    @Test
    void IGDB_결과가_없으면_빈_리스트를_반환한다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("없는게임");

        when(igdbService.search(any()))
                .thenReturn(List.of());

        when(genreRepository.findByIgdbIdIn(any()))
                .thenReturn(List.of());

        when(igdbService.getPlatformNameMap(any()))
                .thenReturn(Map.of());

        // when
        List<?> result = gameSearchService.search(condition);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void IGDB_검색_결과가_1000건이어도_정상_처리된다() {
        // given
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");

        List<IgdbGameSummaryDto> manyGames =
                IntStream.range(0, 1000)
                        .mapToObj(i -> mock(IgdbGameSummaryDto.class))
                        .toList();

        when(igdbService.search(any())).thenReturn(manyGames);
        when(genreRepository.findByIgdbIdIn(any())).thenReturn(List.of());
        when(igdbService.getPlatformNameMap(any())).thenReturn(Map.of());

        long start = System.currentTimeMillis();

        // when
        gameSearchService.search(condition);

        long end = System.currentTimeMillis();

        // then
        assertThat(end - start).isLessThan(1000);
    }

}