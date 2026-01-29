package com.back.domain.member.member.controller;

import com.back.domain.game.game.service.GenreSyncService;
import com.back.domain.member.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {

    @Autowired MockMvc mvc;
    @Autowired MemberRepository memberRepository;

    private final ObjectMapper om = new ObjectMapper();

    // 서버 뜰 때 GenreInitializer(@PostConstruct)에서 syncGenres() 호출하는 거 막기
    @MockitoBean
    GenreSyncService genreSyncService;

    // --- 유니크 데이터 생성 (해결책 A) ---
    private static final String EMAIL_PREFIX = "member_test";
    private static final String EMAIL_DOMAIN = "@test.com";
    private static final String NICK_PREFIX = "테스트닉";

    private String uniqueEmail() {
        return EMAIL_PREFIX + "_" + System.nanoTime() + EMAIL_DOMAIN;
    }

    private String uniqueNickname() {
        return NICK_PREFIX + "_" + System.nanoTime();
    }

    // --- helpers ---
    private void signup(String email, String password, String nickname) throws Exception {
        mvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", email,
                                "password", password,
                                "nickname", nickname
                        ))))
                .andExpect(status().isCreated());
    }

    private Cookie[] loginAndGetCookies(String email, String password) throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotEmpty();
        return cookies;
    }

    // --- tests ---

    @Test
    @DisplayName("닉네임 중복 체크: 사용 가능하면 available=true")
    void checkNickname_available_true() throws Exception {
        mvc.perform(get("/api/v1/members/check-nickname")
                        .param("nickname", uniqueNickname()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("닉네임 중복 체크: 이미 사용 중이면 available=false")
    void checkNickname_available_false() throws Exception {
        String email = uniqueEmail();
        String nickname = "중복닉_" + System.nanoTime(); // 길이 2+ 보장
        signup(email, "1234", nickname);

        mvc.perform(get("/api/v1/members/check-nickname")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.available").value(false));
    }

    @Test
    @DisplayName("닉네임 중복 체크: 빈 값이면 400-1")
    void checkNickname_validation_fail() throws Exception {
        mvc.perform(get("/api/v1/members/check-nickname")
                        .param("nickname", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("/me: 로그인 안 하면 401-1")
    void me_unauthorized() throws Exception {
        mvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("/me: 로그인 하면 내 정보 조회 200-1")
    void me_authorized() throws Exception {
        String email = uniqueEmail();
        String nickname = "나나_" + System.nanoTime();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(get("/api/v1/members/me").cookie(cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname));
    }

    @Test
    @DisplayName("비밀번호 변경: 로그인 안 하면 401-1")
    void changePassword_unauthorized() throws Exception {
        mvc.perform(put("/api/v1/members/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "oldPassword", "1234",
                                "newPassword", "5678"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("비밀번호 변경 성공: 이후 새 비밀번호로 로그인 가능")
    void changePassword_success() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/password")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "oldPassword", "1234",
                                "newPassword", "5678"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"));

        // 새 비번 로그인 성공
        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", email,
                                "password", "5678"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"));

        // 기존 비번 로그인 실패
        mvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", email,
                                "password", "1234"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("비밀번호 변경: oldPassword 틀리면 401-2")
    void changePassword_wrong_old_password() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/password")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "oldPassword", "0000",
                                "newPassword", "5678"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"));
    }

    @Test
    @DisplayName("비밀번호 변경: 새 비번이 기존과 같으면 400-2")
    void changePassword_same_as_old() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/password")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "oldPassword", "1234",
                                "newPassword", "1234"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"));
    }

    @Test
    @DisplayName("비밀번호 변경: validation 실패(빈 값 등)면 400-1")
    void changePassword_validation_fail() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/password")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "oldPassword", "",
                                "newPassword", ""
                        ))))
                // 필요하면 실제 응답을 보기 위해 잠시 켜기
                // .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("닉네임 변경: 로그인 안 하면 401-1")
    void changeNickname_unauthorized() throws Exception {
        mvc.perform(put("/api/v1/members/me/nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "nickname", "새닉네임"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"));
    }

    @Test
    @DisplayName("닉네임 변경 성공: /me 조회 시 변경된 닉네임이 나온다")
    void changeNickname_success() throws Exception {
        String email = uniqueEmail();
        String oldNick = uniqueNickname();
        String newNick = "새닉_" + System.nanoTime();

        signup(email, "1234", oldNick);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/nickname")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "nickname", newNick
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"));

        // 변경 확인: /me
        mvc.perform(get("/api/v1/members/me").cookie(cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(newNick));
    }

    @Test
    @DisplayName("닉네임 변경: validation 실패(빈 값)이면 400-1")
    void changeNickname_validation_fail() throws Exception {
        String email = uniqueEmail();
        String nickname = uniqueNickname();

        signup(email, "1234", nickname);
        Cookie[] cookies = loginAndGetCookies(email, "1234");

        mvc.perform(put("/api/v1/members/me/nickname")
                        .with(csrf())
                        .cookie(cookies)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "nickname", ""
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }
}
