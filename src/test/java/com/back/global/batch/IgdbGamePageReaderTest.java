package com.back.global.batch;

import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.batch.reader.IgdbGamePageReader;
import com.back.support.IgdbFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbGamePageReaderTest {

    @Mock
    private IgdbClient igdbClient;

    @Test
    void 전체_동기화_updatedAfterEpoch가_null이면_필터없이_호출한다() {
        IgdbGamePageReader reader = new IgdbGamePageReader(igdbClient, null);

        when(igdbClient.fetchGamePage(0, 500, null))
                .thenReturn(List.of(IgdbFixtures.gameDetail(1L)));

        IgdbGameDetailDto result = reader.read();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(igdbClient).fetchGamePage(0, 500, null);
    }

    @Test
    void 증분_동기화_updatedAfterEpoch가_있으면_해당값으로_호출한다() {
        long lastSync = 1700000000L;
        IgdbGamePageReader reader = new IgdbGamePageReader(igdbClient, lastSync);

        when(igdbClient.fetchGamePage(0, 500, lastSync))
                .thenReturn(List.of(IgdbFixtures.gameDetail(1L)));

        IgdbGameDetailDto result = reader.read();

        assertThat(result).isNotNull();
        verify(igdbClient).fetchGamePage(0, 500, lastSync);
    }

    @Test
    void 빈_페이지_반환시_null을_반환하고_종료한다() {
        IgdbGamePageReader reader = new IgdbGamePageReader(igdbClient, null);

        when(igdbClient.fetchGamePage(0, 500, null)).thenReturn(List.of());

        assertThat(reader.read()).isNull();
        assertThat(reader.read()).isNull(); // 이후 호출도 null
    }

    @Test
    void 페이지_크기보다_적으면_마지막_페이지로_판단한다() {
        IgdbGamePageReader reader = new IgdbGamePageReader(igdbClient, null);

        // 2건만 반환 (500 미만) → 마지막 페이지
        when(igdbClient.fetchGamePage(0, 500, null))
                .thenReturn(List.of(IgdbFixtures.gameDetail(1L), IgdbFixtures.gameDetail(2L)));

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull(); // exhausted

        // 추가 API 호출 없음
        verify(igdbClient, times(1)).fetchGamePage(anyInt(), anyInt(), any());
    }
}
