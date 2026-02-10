package com.back.global.batch;

import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.batch.dto.GameBatchItem;
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
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private GameModeRepository gameModeRepository;
    @Mock
    private PlayerPerspectiveRepository playerPerspectiveRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private CompanyRepository companyRepository;

    private IgdbGameProcessor processor;

    @BeforeEach
    void setUp() {
        Genre action = Genre.createGenre(10000L, "Action");
        Genre rpg = Genre.createGenre(10001L, "RPG");
        Platform pc = Platform.createPlatform(10001L, "PC (Windows)");
        Platform ps5 = Platform.createPlatform(20001L, "PlayStation 5");
        Company testCompany = Company.createCompany(10000L, "testCompany");
        Company devCo = Company.createCompany(1L, "DevCo");
        Company pubCo = Company.createCompany(2L, "PubCo");

        when(genreRepository.findAll()).thenReturn(List.of(action, rpg));
        when(platformRepository.findAll()).thenReturn(List.of(pc, ps5));
        when(themeRepository.findAll()).thenReturn(List.of());
        when(gameModeRepository.findAll()).thenReturn(List.of());
        when(playerPerspectiveRepository.findAll()).thenReturn(List.of());
        when(keywordRepository.findAll()).thenReturn(List.of());
        when(companyRepository.findAll()).thenReturn(List.of(testCompany, devCo, pubCo));

        processor = new IgdbGameProcessor(
                genreRepository, platformRepository,
                themeRepository, gameModeRepository, playerPerspectiveRepository,
                keywordRepository, companyRepository
        );
    }

    @Test
    void 정상_DTO를_GameBatchItem으로_변환한다() {
        IgdbGameDetailDto dto = IgdbFixtures.gameDetail(1L);

        GameBatchItem item = processor.process(dto);

        assertThat(item).isNotNull();
        assertThat(item.getGame().getIgdbId()).isEqualTo(1L);
        assertThat(item.getGame().getName()).isEqualTo("Test Game 1");
        assertThat(item.getGame().getSummary()).isEqualTo("summary-1");
        assertThat(item.getGame().getCoverImageId()).isEqualTo("coverImage");
        assertThat(item.getCompanies()).hasSize(2); // testCompany as DEVELOPER + PUBLISHER
        assertThat(item.getGenres()).hasSize(2);
        assertThat(item.getPlatforms()).hasSize(2);
    }

    @Test
    void name이_null이면_null을_반환한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, null, "summary", 1700000000L,
                null, null, null, null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item).isNull();
    }

    @Test
    void summary가_null이면_빈문자열로_설정한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", null, 1700000000L,
                null, null, null, null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item.getGame().getSummary()).isEmpty();
    }

    @Test
    void summary가_5000자_초과해도_그대로_유지한다() {
        String longSummary = "A".repeat(6000);
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", longSummary, 1700000000L,
                null, null, null, null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item.getGame().getSummary()).hasSize(6000);
    }

    @Test
    void cover가_null이면_coverImageId가_null이다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", "summary", 1700000000L,
                null, null, null, null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item.getGame().getCoverImageId()).isNull();
    }

    @Test
    void 캐시에_없는_장르는_무시한다() {
        IgdbGameDetailDto dto = new IgdbGameDetailDto(
                1L, "Game", "summary", 1700000000L,
                null, null,
                List.of(new IgdbGenreDto(99999L, "Unknown")),
                null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item.getGenres()).isEmpty();
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
                null, null,
                null, null, null, null, null, null, null, null
        );

        GameBatchItem item = processor.process(dto);

        assertThat(item.getCompanies()).hasSize(2);
        assertThat(item.getCompanies())
                .anyMatch(c -> c.company().getName().equals("DevCo") && c.role() == CompanyRole.DEVELOPER);
        assertThat(item.getCompanies())
                .anyMatch(c -> c.company().getName().equals("PubCo") && c.role() == CompanyRole.PUBLISHER);
    }
}
