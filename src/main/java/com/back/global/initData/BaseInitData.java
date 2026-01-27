package com.back.global.initData;

import com.back.domain.game.game.service.GameService;
import com.back.domain.member.member.service.MemberService;
import com.back.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    private final MemberService memberService;
    private final GameService gameService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> work1();
    }

    @Transactional
    public void work1() {
        if (AppConfig.isProd()) return;

        tryJoin("admin@test.com", "1234", "관리자");
        tryJoin("user1@test.com", "1234", "유저1");
        tryJoin("user2@test.com", "1234", "유저2");
        try {
            gameService.createGame(123L,
                    "Cat Mario",
                    "고양이 마리오 게임",
                    "cat_mario.png",
                    LocalDate.now());
            gameService.createGame(124L,
                    "Super Mario Bros.",
                    "최초의 마리오 게임",
                    "super_mario_bros.png",
                    LocalDate.of(1985, 9,13));
        } catch (Exception ignored) {
        }

    }

    private void tryJoin(String email, String password, String nickname) {
        try {
            memberService.join(email, password, nickname);
        } catch (Exception ignored) {
        }
    }
}
