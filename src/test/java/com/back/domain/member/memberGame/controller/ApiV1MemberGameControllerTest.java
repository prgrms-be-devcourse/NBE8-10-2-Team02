package com.back.domain.member.memberGame.controller;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.service.GameService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.domain.member.memberGame.service.MemberGameService;
import com.back.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiV1MemberGameControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    GameService gameService;

    @MockBean
    MemberService memberService;

    @MockBean
    MemberGameService memberGameService;

    @MockBean
    Rq rq;

    @Test
    @DisplayName("라이브러리 조회 - 성공")
    void t1_viewLibrary_success() throws Exception {
        // Given
        int memberId = 1;
        Member member = new Member(memberId, "test@test.com", "테스터");
        Game game = Game.createGame(1L, "Test Game", "summary", "cover123", LocalDate.now());
        MemberGame memberGame = new MemberGame("PC", 10.5, true, StatusEnum.PLAYING, member, game);

        when(rq.getActor()).thenReturn(member);
        when(memberService.findById(memberId)).thenReturn(Optional.of(member));

        // When & Then
        mockMvc.perform(get("/api/v1/members/{memberId}/library", memberId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("라이브러리에 게임 추가 - 성공")
    void t2_addToLibrary_success() throws Exception {
        // Given
        int memberId = 1;
        Member actor = new Member(memberId, "test@test.com", "테스터");
        Game game = Game.createGame(1L, "Test Game", "summary", "cover123", LocalDate.now());
        MemberGame memberGame = new MemberGame("PC", 10.5, true, StatusEnum.PLAYING, actor, game);

        when(rq.getActor()).thenReturn(actor);
        when(memberService.findById(memberId)).thenReturn(Optional.of(actor));
        when(gameService.findById(1)).thenReturn(Optional.of(game));
        when(memberGameService.addToLibrary(anyString(), anyDouble(), anyBoolean(), any(StatusEnum.class), any(Member.class), any(Game.class)))
                .thenReturn(memberGame);

        String requestBody = """
                {
                    "platform": "PC",
                    "playtime": 10.5,
                    "isFavorite": true,
                    "status": "PLAYING",
                    "gameId": 1
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/members/{memberId}/library", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("201-1"))
                .andExpect(jsonPath("$.message").value("라이브러리에 게임 Test Game가 추가되었습니다."))
                .andExpect(jsonPath("$.data.platform").value("PC"))
                .andExpect(jsonPath("$.data.playtime").value(10.5))
                .andExpect(jsonPath("$.data.isFavorite").value(true))
                .andExpect(jsonPath("$.data.status").value("PLAYING"));
    }

    @Test
    @DisplayName("라이브러리 게임 상태 업데이트 - 성공")
    void t3_updateMemberGame_success() throws Exception {
        // Given
        int memberId = 1;
        int memberGameId = 1;
        Member actor = new Member(memberId, "test@test.com", "테스터");
        Game game = Game.createGame(1L, "Test Game", "summary", "cover123", LocalDate.now());
        MemberGame memberGame = new MemberGame("PC", 10.5, true, StatusEnum.PLAYING, actor, game);

        when(rq.getActor()).thenReturn(actor);
        when(memberGameService.updateMemberGame(eq(memberGameId), eq(memberId), any()))
                .thenReturn(memberGame);

        String requestBody = """
                {
                    "platform": "PS5",
                    "playtime": 20,
                    "isFavorite": false,
                    "status": "COMPLETED"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/v1/members/{memberId}/library/{memberGameId}", memberId, memberGameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value("게임 정보가 업데이트되었습니다."));
    }

    @Test
    @DisplayName("라이브러리에서 게임 삭제 - 성공")
    void t4_removeFromLibrary_success() throws Exception {
        // Given
        int memberId = 1;
        int memberGameId = 1;
        Member actor = new Member(memberId, "test@test.com", "테스터");

        when(rq.getActor()).thenReturn(actor);
        when(memberService.findById(memberId)).thenReturn(Optional.of(actor));
        when(memberGameService.removeFromLibrary(any(Member.class), eq(memberGameId)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/members/{memberId}/library/{memberGameId}", memberId, memberGameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("204"))
                .andExpect(jsonPath("$.message").value("게임을 삭제하였습니다."));
    }

    @Test
    @DisplayName("라이브러리 조회 - 권한 없음 실패")
    void t5_viewLibrary_unauthorized() throws Exception {
        // Given
        int memberId = 1;
        int otherMemberId = 2;
        Member actor = new Member(otherMemberId, "other@test.com", "다른사용자");

        when(rq.getActor()).thenReturn(actor);

        // When & Then
        mockMvc.perform(get("/api/v1/members/{memberId}/library", memberId))
                .andExpect(status().is5xxServerError()); // Will throw ServiceException
    }

    @Test
    @DisplayName("라이브러리에 게임 추가 - 권한 없음 실패")
    void t6_addToLibrary_unauthorized() throws Exception {
        // Given
        int memberId = 1;
        int otherMemberId = 2;
        Member actor = new Member(otherMemberId, "other@test.com", "다른사용자");

        when(rq.getActor()).thenReturn(actor);

        String requestBody = """
                {
                    "platform": "PC",
                    "playtime": 10.5,
                    "isFavorite": true,
                    "status": "PLAYING",
                    "gameId": 1
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/members/{memberId}/library", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is5xxServerError()); // Will throw ServiceException
    }

    @Test
    @DisplayName("라이브러리 게임 업데이트 - 권한 없음 실패")
    void t7_updateMemberGame_unauthorized() throws Exception {
        // Given
        int memberId = 1;
        int memberGameId = 1;
        int otherMemberId = 2;
        Member actor = new Member(otherMemberId, "other@test.com", "다른사용자");

        when(rq.getActor()).thenReturn(actor);

        String requestBody = """
                {
                    "platform": "PS5",
                    "playtime": 20,
                    "isFavorite": false,
                    "status": "COMPLETED"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/v1/members/{memberId}/library/{memberGameId}", memberId, memberGameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is5xxServerError()); // Will throw ServiceException
    }
}
