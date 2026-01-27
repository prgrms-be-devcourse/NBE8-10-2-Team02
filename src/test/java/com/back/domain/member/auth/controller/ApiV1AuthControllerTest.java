package com.back.domain.member.auth.controller;

import com.back.domain.game.game.service.GenreSyncService;
import com.back.domain.member.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1AuthControllerTest {

    @Autowired MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();
    @Autowired MemberRepository memberRepository;

    @MockitoBean
    GenreSyncService genreSyncService;

    @BeforeEach
    void clear() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일 중복 체크: 가입 전이면 available=true")
    void checkEmail_available_true() throws Exception {
        mvc.perform(get("/api/v1/auth/check-email")
                        .param("email", "newuser@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("회원가입 성공: DB에 회원이 저장된다")
    void signup_success() throws Exception {
        var body = Map.of(
                "email", "user1@test.com",
                "password", "1234",
                "nickname", "유저1"
        );

        mvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"));

        assertThat(memberRepository.findByEmail("user1@test.com")).isPresent();
    }

    @Test
    @DisplayName("로그인 성공: Set-Cookie에 apiKey/accessToken이 내려온다")
    void login_sets_cookies() throws Exception {
        // 1) 먼저 가입
        mvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", "user2@test.com",
                                "password", "1234",
                                "nickname", "유저2"
                        ))))
                .andExpect(status().isCreated());

        // 2) 로그인
        var result = mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", "user2@test.com",
                                "password", "1234"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders("Set-Cookie");
        String all = String.join(";", setCookies);

        assertThat(all).contains("apiKey=");
        assertThat(all).contains("accessToken=");
    }

    @Test
    @DisplayName("로그아웃: 쿠키 삭제(Set-Cookie Max-Age=0) 내려온다")
    void logout_deletes_cookies() throws Exception {
        var result = mvc.perform(post("/api/v1/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders("Set-Cookie");
        String all = String.join(";", setCookies);

        assertThat(all).contains("Max-Age=0");
    }
}
