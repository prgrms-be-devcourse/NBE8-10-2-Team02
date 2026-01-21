package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // api 아니면 패스
        if (!uri.startsWith("/api/")) return true;

        // 인증 예외 경로들 패스(네 SecurityConfig permitAll과 맞추기)
        if (uri.startsWith("/api/v1/auth/")) return true;
        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(Ut.json.toString(rsData));
        }
    }

    private void work(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String authorization = request.getHeader("Authorization");

        // 헤더가 없으면 그냥 통과 -> SecurityConfig가 막으면 401
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        }

        // Bearer {accessToken}  또는  Bearer {apiKey} {accessToken}
        String[] bits = authorization.split(" ", 3);

        String apiKey = "";
        String accessToken = "";

        if (bits.length == 2) {
            accessToken = bits[1].trim();
        } else if (bits.length == 3) {
            apiKey = bits[1].trim();
            accessToken = bits[2].trim();
        }

        // 1) accessToken 우선 검증
        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                int id = (int) payload.get("id");
                String email = (String) payload.get("email");
                String nickname = (String) payload.get("nickname");

                setAuthentication(id, email, nickname);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2) 토큰이 무효면 apiKey로 fallback
        if (apiKey.isBlank()) {
            throw new ServiceException("401-4", "토큰이 유효하지 않습니다.");
        }

        Member member = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));

        setAuthentication(member.getId(), member.getEmail(), member.getNickname());

        // (선택) 토큰 재발급해서 응답 헤더로 내려주기
        String newAccessToken = memberService.genAccessToken(member);
        response.setHeader("Authorization", "Bearer " + member.getApiKey() + " " + newAccessToken);

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(int id, String email, String nickname) {
        UserDetails user = new SecurityUser(id, email, "", nickname, java.util.List.of());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
