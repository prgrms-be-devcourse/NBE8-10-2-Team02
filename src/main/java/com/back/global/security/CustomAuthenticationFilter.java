package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final Rq rq;

    /**
     * 필터 적용 제외 경로
     * - /api 가 아니면 패스
     * - 인증 관련(/api/v1/auth/**), swagger, h2-console 은 패스
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")) return true;

        if (uri.startsWith("/api/v1/auth/")) return true;
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) return true;
        if (uri.startsWith("/h2-console")) return true;



        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            work(); // 인증 처리
            filterChain.doFilter(request, response);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(Ut.json.toString(rsData));
        }
    }

    /**
     * 인증 흐름
     * 1) Authorization 헤더가 있으면 우선 사용
     *    - "Bearer {accessToken}" 또는 "Bearer {apiKey} {accessToken}"
     * 2) Authorization 헤더가 없으면 쿠키(apiKey/accessToken) 사용
     * 3) accessToken 유효하면 그걸로 인증 세팅
     * 4) accessToken이 없거나 유효하지 않으면 apiKey로 회원 조회 후 인증 세팅 + accessToken 재발급(쿠키 갱신)
     */
    private void work() {
        String apiKey = "";
        String accessToken = "";

        String authorization = rq.getHeader("Authorization", "");

        if (!authorization.isBlank()) {
            if (!authorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");
            }

            // Bearer <accessToken>
            // Bearer <apiKey> <accessToken>
            String[] bits = authorization.split(" ", 3);

            if (bits.length == 2) {
                accessToken = bits[1].trim();
            } else if (bits.length == 3) {
                apiKey = bits[1].trim();
                accessToken = bits[2].trim();
            }
        } else {
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
        }

        boolean hasApiKey = !apiKey.isBlank();
        boolean hasAccessToken = !accessToken.isBlank();

        // 둘 다 없으면 인증 시도 자체를 하지 않고 다음 필터로 넘김
        if (!hasApiKey && !hasAccessToken) return;

        // accessToken이 있으면 우선 검증
        if (hasAccessToken) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                // 주의: payload map에서 숫자가 Integer/Long 등으로 올 수 있음
                int id = ((Number) payload.get("id")).intValue();
                String email = (String) payload.get("email");
                String nickname = (String) payload.get("nickname");

                setAuthentication(id, email, nickname);
                return;
            }
        }

        // accessToken이 유효하지 않았는데 apiKey도 없으면 실패
        if (!hasApiKey) {
            throw new ServiceException("401-4", "토큰이 유효하지 않습니다.");
        }

        // apiKey로 회원 조회
        Member member = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));

        setAuthentication(member.getId(), member.getEmail(), member.getNickname());

        // 새 accessToken 발급 + 쿠키 갱신
        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);
    }

    private void setAuthentication(int id, String email, String nickname) {
        UserDetails user = new SecurityUser(
                id,
                email,
                nickname,
                "",
                java.util.List.of()
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        user.getPassword(),
                        user.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
