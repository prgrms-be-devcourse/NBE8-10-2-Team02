package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.*;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbVideoDto;
import com.back.support.IgdbFixtures;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    @Autowired
    GenreRepository genreRepository;
    @Autowired
    PlatformRepository platformRepository;
    @Autowired
    GameGenreRepository gameGenreRepository;
    @Autowired
    GamePlatformRepository gamePlatformRepository;
    @Autowired
    Cache<Long, GameDetailResponse> gameDetailCache;

    @BeforeEach
    void setUp() {
        gameDetailCache.invalidateAll();
    }

    @Test
    @DisplayName("게임상세조회 - API 호출 후 DB 저장")
    void t1_getGameDetail_apiCall_andPersist() {
        //given
        long igdbId = 999L;
        when(igdbClient.getGameDetail(igdbId)).thenReturn(IgdbFixtures.gameDetail(igdbId));

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.gameName()).isEqualTo("Test Game 999");

        // DB에 저장됐는지 확인
        Game saved = gameRepository.findByIgdbId(igdbId).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Test Game 999");

        // API는 1번만 호출됨
        verify(igdbClient, times(1)).getGameDetail(igdbId);
    }

    @Test
    @DisplayName("게임상세조회 - 두 번째 호출은 캐시에서 반환")
    void t2_getGameDetail_secondCall_fromCache() {
        //given
        long igdbId = 888L;
        when(igdbClient.getGameDetail(igdbId)).thenReturn(IgdbFixtures.gameDetail(igdbId));

        //when
        gameService.getGameDetail(igdbId); // 1차: API 호출
        GameDetailResponse result = gameService.getGameDetail(igdbId); // 2차: 캐시

        //then
        assertThat(result.gameName()).isEqualTo("Test Game 888");
        verify(igdbClient, times(1)).getGameDetail(igdbId); // API는 1번만
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
    @DisplayName("비슷한 게임 조회 - API 호출")
    void t5_getSimilarGames_apiCall() {
        //given
        long igdbId = 555L;
        List<Long> similarIds = List.of(1L, 2L, 3L);
        when(igdbClient.getSimilarGameIds(igdbId)).thenReturn(similarIds);
        when(igdbClient.getSimilarGameBriefById(similarIds)).thenReturn(List.of(
                new SimilarGameResponse(1L, "Similar1", "co1"),
                new SimilarGameResponse(2L, "Similar2", "co2")
        ));

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Similar1");
    }

    @Test
    @DisplayName("비슷한 게임 조회 - 두 번째 호출은 캐시에서 반환")
    void t6_getSimilarGames_secondCall_fromCache() {
        //given
        long igdbId = 444L;
        when(igdbClient.getSimilarGameIds(igdbId)).thenReturn(List.of(1L));
        when(igdbClient.getSimilarGameBriefById(anyList())).thenReturn(List.of(
                new SimilarGameResponse(1L, "Cached", "co")
        ));

        //when
        gameService.getSimilarGames(igdbId); // 1차
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId); // 2차

        //then
        assertThat(result).hasSize(1);
        verify(igdbClient, times(1)).getSimilarGameIds(igdbId);
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
        when(igdbClient.getSimilarGameIds(igdbId)).thenReturn(List.of());

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
        Game existingGame = Game.createGame(
                igdbId,
                "Existing Game",
                "Already in DB",
                List.of("Dev Studio"),
                List.of("Publisher Inc"),
                "existingCover",
                1700000000L
        );
        gameRepository.save(existingGame); // DB에 해당 게임이 이미 있다고 가정
        gameDetailCache.invalidate(igdbId); //캐시 비우기

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.gameName()).isEqualTo("Existing Game");

        verify(igdbClient, never()).getGameDetail(igdbId); // api호출 없음
    }

    @Test
    @DisplayName("게임상세조회 - DB의 데이터가 stale이면 API 재호출")
    void t10_getGameDetail_staleData_refetch() throws Exception {
        //given DB에 8일 전 데이터 존재 (stale)
        long igdbId = 1010L;
        Game staleGame = Game.createGame(
                igdbId,
                "Stale Game",
                "Old summary",
                List.of("Old Dev"),
                List.of("Old Publisher"),
                "oldCover",
                1700000000L
        );
        gameRepository.save(staleGame);

        // lastFetchedAt을 8일 전으로 변경 (리플렉션)
        Field lastFetchedAtField = Game.class.getDeclaredField("lastFetchedAt");
        lastFetchedAtField.setAccessible(true);
        lastFetchedAtField.set(staleGame, Instant.now().minus(8, ChronoUnit.DAYS));
        gameRepository.save(staleGame);

        gameDetailCache.invalidate(igdbId); //캐시 비우기
        when(igdbClient.getGameDetail(igdbId)).thenReturn(IgdbFixtures.gameDetail(igdbId));

        //when
        GameDetailResponse result = gameService.getGameDetail(igdbId);

        //then
        assertThat(result.gameName()).isEqualTo("Test Game " + igdbId);
        verify(igdbClient, times(1)).getGameDetail(igdbId);
    }
    
    @Test
    @DisplayName("게임상세조회 - API가 null 반환하면 예외 발생")
    public void t11_getGameDetail_notFound_throwsException() {
        //given
        long igdbId = 1011L;
        when(igdbClient.getGameDetail(igdbId)).thenReturn(null);

        //when, then
        assertThatThrownBy(() -> gameService.getGameDetail(igdbId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("게임을 찾을 수 없습니다. " + igdbId);
    }

    @Test
    @DisplayName("비디오 조회 - dto는 있지만 videoId가 null이면 빈 문자열 반환")
    public void t12_getVideoId_videoIdNull_returnsEmpty() {
        //given
        long igdbId = 1012L;
        when(igdbClient.getVideoId(igdbId)).thenReturn(new IgdbVideoDto(1001L, null));
        
        //when
        GameVideoResponse result = gameService.getVideoId(igdbId);

        //then
        assertThat(result.videoId()).isEmpty();
    }
    
    @Test
    @DisplayName("게임상세조회 - Genre가 정상적으로 연결됨")
    public void t13_getGameDetail_genresLinked() {
        //given
        long igdbId = 1013L;
        when(igdbClient.getGameDetail(igdbId)).thenReturn(IgdbFixtures.gameDetail(igdbId));

        //when
        gameService.getGameDetail(igdbId);
        
        //then
        Game savedGame = gameRepository.findByIgdbId(igdbId).orElseThrow();
        List<String> linkedGenres = gameGenreRepository.findGenreNamesByGameId(savedGame.getId());

        assertThat(linkedGenres).containsExactlyInAnyOrder("Action", "RPG");
    }

    @Test
    @DisplayName("게임상세조회 - Platform이 정상적으로 연결됨")
    void t14_getGameDetail_platformsLinked() {
        //given
        long igdbId = 1014L;
        when(igdbClient.getGameDetail(igdbId)).thenReturn(IgdbFixtures.gameDetail(igdbId));

        //when
        gameService.getGameDetail(igdbId);

        //then
        Game savedGame = gameRepository.findByIgdbId(igdbId).orElseThrow();
        List<String> linkedPlatforms = gamePlatformRepository.findPlatformNamesByGameId(savedGame.getId());

        assertThat(linkedPlatforms).containsExactlyInAnyOrder("PC (Windows)", "PlayStation 5");
    }

    @Test
    @DisplayName("비슷한 게임 조회 - similarIds가 null이면 빈 리스트 반환")
    void t15_getSimilarGames_nullIds_returnsEmptyList() {
        //given
        long igdbId = 1015L;
        when(igdbClient.getSimilarGameIds(igdbId)).thenReturn(null);

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).isEmpty();
        verify(igdbClient, never()).getSimilarGameBriefById(anyList());
    }

    @Test
    @DisplayName("비슷한 게임 조회 - briefList가 null이면 빈 리스트 반환")
    void t16_getSimilarGames_nullBriefList_returnsEmptyList() {
        //given
        long igdbId = 1016L;
        when(igdbClient.getSimilarGameIds(igdbId)).thenReturn(List.of(1L, 2L));
        when(igdbClient.getSimilarGameBriefById(anyList())).thenReturn(null);

        //when
        List<SimilarGameResponse> result = gameService.getSimilarGames(igdbId);

        //then
        assertThat(result).isEmpty();
    }
}