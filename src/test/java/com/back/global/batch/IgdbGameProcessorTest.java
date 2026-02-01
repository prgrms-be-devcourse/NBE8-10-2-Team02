package com.back.global.batch;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.entity.Platform;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.domain.game.game.repository.PlatformRepository;
import com.back.global.batch.processor.IgdbGameProcessor;
import com.back.global.igdb.dto.*;
import com.back.support.IgdbFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IgdbGameProcessorTest {

    @Mock
    private GenreRepository genreRepository;
    @Mock
    private PlatformRepository platformRepository;

    private IgdbGameProcessor processor;

    @BeforeEach
    void setUp() {
        Genre action = Genre.createGenre(10000L, "Action");
        Genre rpg = Genre.createGenre(10001L, "RPG");
        Platform pc = Platform.createPlatform(10001L, "PC (Windows)");
        Platform ps5 = Platform.createPlatform(20001L, "PlayStation 5");

        when(genreRepository.findAll()).thenReturn(List.of(action, rpg));
        when(platformRepository.findAll()).thenReturn(List.of(pc, ps5));

        processor = new IgdbGameProcessor(genreRepository, platformRepository);
    }

    @Test
    void 정상_DTO를_Game_엔티티로_변환한다() {
        IgdbGameDetailDto dto = IgdbFixtures.gameDetail(1L);

        Game game = processor.process(dto);

        assertThat(game).isNotNull();
        assertThat(game.getIgdbId()).isEqualTo(1L);
        assertThat(game.getName()).isEqualTo("Test Game 1");
        assertThat(game.getSummary()).isEqualTo("summary-1");
        assertThat(game.getCoverImageId()).isEqualTo("coverImage");
        assertThat(game.getDevelopers()).containsExactly("testCompany");
        assertThat(game.getPublishers()).containsExactly("testCompany");
        assertThat(game.getGameGenres()).hasSize(2);
        assertThat(game.getGamePlatforms()).hasSize(2);
    }

    @Test
    void name이_null이면_null을_반환한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, null, "summary", 1700000000L,
                null, null, null, null
        );

        Game game = processor.process(dto);

        assertThat(game).isNull();
    }

    @Test
    void summary가_null이면_빈문자열로_설정한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", null, 1700000000L,
                null, null, null, null
        );

        Game game = processor.process(dto);

        assertThat(game.getSummary()).isEmpty();
    }

    @Test
    void summary가_5000자_초과면_잘라낸다() {
        String longSummary = "A".repeat(6000);
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", longSummary, 1700000000L,
                null, null, null, null
        );

        Game game = processor.process(dto);

        assertThat(game.getSummary()).hasSize(5000);
    }

    @Test
    void cover가_null이면_coverImageId가_null이다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", "summary", 1700000000L,
                null, null, null, null
        );

        Game game = processor.process(dto);

        assertThat(game.getCoverImageId()).isNull();
    }

    @Test
    void 캐시에_없는_장르는_무시한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", "summary", 1700000000L,
                null, null,
                List.of(new IgdbGenreDto(99999L, "Unknown")),
                null
        );

        Game game = processor.process(dto);

        assertThat(game.getGameGenres()).isEmpty();
    }

    @Test
    void involvedCompanies에서_developer와_publisher를_분리한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", "summary", 1700000000L,
                null,
                List.of(
                        new IgdbInvolvedCompanyDto(1L, new IgdbCompanyDto(1L, "DevCo"), true, false),
                        new IgdbInvolvedCompanyDto(2L, new IgdbCompanyDto(2L, "PubCo"), false, true)
                ),
                null, null
        );

        Game game = processor.process(dto);

        assertThat(game.getDevelopers()).containsExactly("DevCo");
        assertThat(game.getPublishers()).containsExactly("PubCo");
    }
}
