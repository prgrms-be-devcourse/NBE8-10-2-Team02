package com.back.global.initData;

import com.back.domain.member.member.service.MemberService;
import com.back.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    private final MemberService memberService;

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
    }

    private void tryJoin(String email, String password, String nickname) {
        try {
            memberService.join(email, password, nickname);
        } catch (Exception ignored) {
        }
    }
}
