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
                // 기본 보안 설정
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

        // ==========================================
        // ✅ 정상 코드(권한 정책 적용)
        // - 조회(GET)는 공개
        // - 쓰기/수정/삭제(POST/PUT/DELETE)는 로그인 필요
        // ==========================================
        http.authorizeHttpRequests(auth -> auth
                // preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // swagger / h2
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // auth
                .requestMatchers("/api/v1/auth/**").permitAll()

                // 닉네임/이메일 중복 체크(네 코드 기준)
                .requestMatchers(HttpMethod.GET, "/api/v1/members/check-nickname").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/check-email").permitAll()

                // ======================
                // 게임 조회/검색 (공개)
                // ======================
                .requestMatchers(HttpMethod.GET, "/api/v1/games/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/genres/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/platforms/**").permitAll()
                // ======================
                // 게시글/댓글 조회 (공개)
                // ======================
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/**").permitAll()

                // ======================
                // 리뷰 조회 (공개)
                // ======================
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/game/").permitAll()

                // 그 외 /api/** 는 로그인 필요
                .requestMatchers("/api/**").authenticated()

                // 나머지는 열어둠(정적 리소스 등)
                .anyRequest().permitAll()
        );

        /*
        // ==========================================
        // 🧪 테스트용(임시): 로그인 없이 전부 허용
        // - 팀원들이 초반에 Postman으로 편하게 테스트할 때만 사용
        // - 사용할 땐 위 "정상 코드" authorizeHttpRequests 블록을 주석 처리하고
        //   이 블록을 켜서 사용
        // ==========================================
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll()
        );
        */

        return http.build();
    }
}
