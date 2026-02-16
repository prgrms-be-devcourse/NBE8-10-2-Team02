package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.domain.game.recommendation.dto.GameRecommendationResponse;
import com.back.domain.game.recommendation.service.GameRecommendationService;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbVideoDto;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired
    GameService gameService;
    @Autowired
    GameRepository gameRepository;
    @MockitoBean
    IgdbClient igdbClient;
    @MockitoBean
    GameRecommendationService gameRecommendationService;
    @Autowired
    GenreRepository genreRepository;
    @Autowired
    PlatformRepository platformRepository;
    @Autowired
    GameGenreRepository gameGenreRepository;
    @Autowired
    GamePlatformRepository gamePlatformRepository;
    @Autowired
    GameCompanyRepository gameCompanyRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    Cache<Long, GameDetailResponse> gameDetailCache;
    @Autowired
    Cache<Long, List<SimilarGameResponse>> similarListCache;

    @BeforeEach
    void setUp() {
        gameDetailCache.invalidateAll();
        similarListCache.invalidateAll();
    }

    @Test
    @DisplayName("게임상세조회 - DB에서 조회 성공")
    void t1_getGameDetail_fromDb() {
        //given
        long igdbId = 999L;
        Game game = Game.createGame(igdbId, "Test Game 999", "summary-999",
                "coverImage", 1700000000L, null, null, null, null);
        gameRepository.save(game);

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.gameName()).isEqualTo("Test Game 999");
        verify(igdbClient, never()).getGameDetail(anyLong());
    }

    @Test
    @DisplayName("게임상세조회 - 두 번째 호출은 캐시에서 반환")
    void t2_getGameDetail_secondCall_fromCache() {
        //given
        long igdbId = 888L;
        Game game = Game.createGame(igdbId, "Test Game 888", "summary-888",
                "coverImage", 1700000000L, null, null, null, null);
        gameRepository.save(game);

        //when
        gameService.getGameDetail(igdbId); // 1차: DB 조회
        GameDetailResponse result = gameService.getGameDetail(igdbId); // 2차: 캐시

        //then
        assertThat(result.gameName()).isEqualTo("Test Game 888");
    }

    @Test
    @DisplayName("비디오 조회 - API 호출")
    void t3_getVideoId_apiCall() {
        //given
        long igdbId = 777L;
        when(igdbClient.getVideoId(igdbId)).thenReturn(new IgdbVideoDto(1L, "video123"));

        //when
        GameVideoResponse result = gameService.getVideoId(igdbId);

        //then
        assertThat(result.videoId()).isEqualTo("video123");
        verify(igdbClient, times(1)).getVideoId(igdbId);
    }

    @Test
    @DisplayName("비디오 조회 - 두 번째 호출은 캐시에서 반환")
    void t4_getVideoId_secondCall_fromCache() {
        //given
        long igdbId = 666L;
        when(igdbClient.getVideoId(igdbId)).thenReturn(new IgdbVideoDto(1L, "cachedVideo"));

        //when
        gameService.getVideoId(igdbId); // 1차
        GameVideoResponse result = gameService.getVideoId(igdbId); // 2차

        //then
        assertThat(result.videoId()).isEqualTo("cachedVideo");
        verify(igdbClient, times(1)).getVideoId(igdbId);
    }

    @Test
    @DisplayName("비슷한 게임 조회 - pgvector 추천 호출")
    void t5_getSimilarGames_recommendation() {
        //given
        long igdbId = 555L;
        when(gameRecommendationService.getSimilarGames(igdbId, 10)).thenReturn(List.of(
                new GameRecommendationResponse(1, "Similar1", "co1", 0.9, 0.85),
                new GameRecommendationResponse(2, "Similar2", "co2", 0.8, 0.75)
        ));

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Similar1");
        verify(gameRecommendationService, times(1)).getSimilarGames(igdbId, 10);
    }

    @Test
    @DisplayName("비슷한 게임 조회 - 두 번째 호출은 캐시에서 반환")
    void t6_getSimilarGames_secondCall_fromCache() {
        //given
        long igdbId = 444L;
        when(gameRecommendationService.getSimilarGames(igdbId, 10)).thenReturn(List.of(
                new GameRecommendationResponse(1, "Cached", "co", 0.9, 0.85)
        ));

        //when
        gameService.getSimilarGames(igdbId); // 1차
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId); // 2차

        //then
        assertThat(result).hasSize(1);
        verify(gameRecommendationService, times(1)).getSimilarGames(igdbId, 10);
    }

    @Test
    @DisplayName("비디오 없으면 - 빈 문자열 반환")
    void t7_getVideoId_notFound_returnsEmpty() {
        //given
        long igdbId = 333L;
        when(igdbClient.getVideoId(igdbId)).thenReturn(null);

        //when
        GameVideoResponse result = gameService.getVideoId(igdbId);

        //then
        assertThat(result.videoId()).isEmpty();
    }

    @Test
    @DisplayName("비슷한 게임 없으면 - 빈 리스트 반환")
    void t8_getSimilarGames_notFound_returnsEmptyList() {
        //given
        long igdbId = 222L;
        when(gameRecommendationService.getSimilarGames(igdbId, 10)).thenReturn(List.of());

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("게임상세조회 - 캐시 miss, DB hit 일때 API호출 안 함")
    void t9_getGameDetail_dbHit_noApiCall() {
        //given
        long igdbId = 1009L;
        Game existingGame = Game.createGame(igdbId, "Existing Game", "Already in DB",
                "existingCover", 1700000000L, null, null, null, null);
        gameRepository.save(existingGame);
        gameDetailCache.invalidate(igdbId);

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.gameName()).isEqualTo("Existing Game");
        verify(igdbClient, never()).getGameDetail(igdbId);
    }

    @Test
    @DisplayName("게임상세조회 - DB에 없으면 예외 발생")
    void t10_getGameDetail_notFound_throwsException() {
        //given
        long igdbId = 1011L;

        //when, then
        assertThatThrownBy(() -> gameService.getGameDetail(igdbId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("게임을 찾을 수 없습니다. " + igdbId);
    }

    @Test
    @DisplayName("비디오 조회 - dto는 있지만 videoId가 null이면 빈 문자열 반환")
    void t11_getVideoId_videoIdNull_returnsEmpty() {
        //given
        long igdbId = 1012L;
        when(igdbClient.getVideoId(igdbId)).thenReturn(new IgdbVideoDto(1001L, null));

        //when
        GameVideoResponse result = gameService.getVideoId(igdbId);

        //then
        assertThat(result.videoId()).isEmpty();
    }

    @Test
    @DisplayName("게임상세조회 - Genre가 정상적으로 반환됨")
    void t12_getGameDetail_genresReturned() {
        //given
        long igdbId = 1013L;
        Game game = Game.createGame(igdbId, "Genre Game", "summary",
                "cover", 1700000000L, null, null, null, null);
        gameRepository.save(game);

        Genre action = genreRepository.save(Genre.createGenre(10000L, "Action"));
        Genre rpg = genreRepository.save(Genre.createGenre(10001L, "RPG"));
        gameGenreRepository.save(GameGenre.createGameGenre(game, action));
        gameGenreRepository.save(GameGenre.createGameGenre(game, rpg));

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result.genres()).containsExactlyInAnyOrder("Action", "RPG");
    }

    @Test
    @DisplayName("게임상세조회 - Platform이 정상적으로 반환됨")
    void t13_getGameDetail_platformsReturned() {
        //given
        long igdbId = 1014L;
        Game game = Game.createGame(igdbId, "Platform Game", "summary",
                "cover", 1700000000L, null, null, null, null);
        gameRepository.save(game);

        Platform pc = platformRepository.save(Platform.createPlatform(10001L, "PC (Windows)"));
        Platform ps5 = platformRepository.save(Platform.createPlatform(20001L, "PlayStation 5"));
        gamePlatformRepository.save(GamePlatform.createGamePlatform(game, pc));
        gamePlatformRepository.save(GamePlatform.createGamePlatform(game, ps5));

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result.platforms()).containsExactlyInAnyOrder("PC (Windows)", "PlayStation 5");
    }

    @Test
    @DisplayName("게임상세조회 - Developer/Publisher가 정상적으로 반환됨")
    void t14_getGameDetail_companiesReturned() {
        //given
        long igdbId = 1015L;
        Game game = Game.createGame(igdbId, "Company Game", "summary",
                "cover", 1700000000L, null, null, null, null);
        gameRepository.save(game);

        Company company = companyRepository.save(Company.createCompany(10000L, "Nintendo"));
        gameCompanyRepository.save(GameCompany.createGameCompany(game, company, CompanyRole.DEVELOPER));
        gameCompanyRepository.save(GameCompany.createGameCompany(game, company, CompanyRole.PUBLISHER));

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result.developers()).containsExactly("Nintendo");
        assertThat(result.publishers()).containsExactly("Nintendo");
    }

    @Test
    @DisplayName("비슷한 게임 조회 - 추천 결과가 빈 리스트이면 빈 리스트 반환")
    void t15_getSimilarGames_emptyRecommendations_returnsEmptyList() {
        //given
        long igdbId = 1016L;
        when(gameRecommendationService.getSimilarGames(igdbId, 10)).thenReturn(List.of());

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).isEmpty();
    }
}
