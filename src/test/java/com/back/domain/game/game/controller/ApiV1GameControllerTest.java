package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.service.GameService;
import com.back.global.exception.ServiceException;
import com.back.global.igdb.IgdbClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1GameControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GameService gameService;
    @MockitoBean
    IgdbClient igdbClient;

    @Test
    @DisplayName("게임상세조회 - 성공")
    void t1_getGameDetail_success() throws Exception {
        long igdbId = 10L;
        Game game = Game.createGame(igdbId, "Zelda", "summary",
                "co", null, null, null, null, null);

        Field idField = Game.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(game, 1);

        when(gameService.getGameDetail(igdbId))
                .thenReturn(GameDetailResponse.from(game, List.of("Action"), List.of("Switch"),
                        List.of("Nintendo"), List.of("Nintendo")));

        mockMvc.perform(get("/api/v1/games/{igdbId}", igdbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.igdbId").value(10))
                .andExpect(jsonPath("$.gameName").value("Zelda"))
                .andExpect(jsonPath("$.coverImageId").value("co"))
                .andExpect(jsonPath("$.developers[0]").value("Nintendo"))
                .andExpect(jsonPath("$.publishers[0]").value("Nintendo"))
                .andExpect(jsonPath("$.genres[0]").value("Action"))
                .andExpect(jsonPath("$.platforms[0]").value("Switch"));
    }


    @Test
    @DisplayName("게임상세조회 - 없는 게임")
    void t2_getGameDetail_notFound() throws Exception {
        when(gameService.getGameDetail(999L))
                .thenThrow(new ServiceException("404-1", "게임을 찾을 수 없습니다. " + 999L));

        mockMvc.perform(get("/api/v1/games/{igdbId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("비디오 조회 - 성공")
    void t3_getVideoId_success() throws Exception {
        when(gameService.getVideoId(10L))
                .thenReturn(GameVideoResponse.from("abc123"));

        mockMvc.perform(get("/api/v1/games/{igdbId}/video", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoId").value("abc123"));
    }

    @Test
    @DisplayName("비슷한 게임 조회 - 성공")
    void t4_getSimilarGames_success() throws Exception {
        when(gameService.getSimilarGames(10L))
                .thenReturn(List.of(
                        new SimilarGameResponse(20L, "Similar Game", "cover123")
                ));

        mockMvc.perform(get("/api/v1/games/{igdbId}/similarGames", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Similar Game"));
    }

}
