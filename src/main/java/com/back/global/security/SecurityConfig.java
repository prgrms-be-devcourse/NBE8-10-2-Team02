package com.back.global.security;

import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // ✅ 쿠키/Authorization 기반 인증 처리 필터
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ 인증/권한 실패 시 RsData(JSON)로 통일해서 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write(
                                    Ut.json.toString(new RsData<Void>("401-1", "로그인 후 이용해주세요."))
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write(
                                    Ut.json.toString(new RsData<Void>("403-1", "권한이 없습니다."))
                            );
                        })
                );

        // =========================
        // ✅ 정상 코드(최종): /api/** 는 로그인 필요
        // =========================
        // http.authorizeHttpRequests(auth -> auth
        //         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        //         .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        //         .requestMatchers("/h2-console/**").permitAll()
        //         .requestMatchers("/api/v1/auth/**").permitAll()
        //         .requestMatchers(HttpMethod.GET, "/api/v1/members/check-nickname").permitAll()
        //         .requestMatchers("/api/**").authenticated()
        //         .anyRequest().permitAll()
        // );

        // =========================
        // ⚠️ 테스트용(임시): 로그인 없이 전부 허용
        // - 프론트/포스트맨 개발 초반 편의를 위한 설정
        // - 테스트 종료 후 위 "정상 코드"로 되돌릴 것
        // =========================
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll()
        );

        return http.build();
    }
}
