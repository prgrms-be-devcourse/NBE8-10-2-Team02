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
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final Rq rq;

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
            work(response);
            filterChain.doFilter(request, response);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(Ut.json.toString(rsData));
        }
    }

    private void work(HttpServletResponse response) {
        String apiKey = "";
        String accessToken = "";

        String authorization = rq.getHeader("Authorization", "");

        if (!authorization.isBlank()) {
            if (!authorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");
            }

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

        if (!hasApiKey && !hasAccessToken) return;

        if (hasAccessToken) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                int id = (int) payload.get("id");
                String email = (String) payload.get("email");
                String nickname = (String) payload.get("nickname");

                setAuthentication(id, email, nickname);
                return;
            }
        }

        if (!hasApiKey) {
            throw new ServiceException("401-4", "토큰이 유효하지 않습니다.");
        }

        Member member = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));

        setAuthentication(member.getId(), member.getEmail(), member.getNickname());

        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);
    }

    private void setAuthentication(int id, String email, String nickname) {
        // SecurityUser(id, email, nickname, password, authorities)
        UserDetails user = new SecurityUser(
                id,
                email,
                nickname,
                "",                 // password (안 쓰면 빈 문자열)
                java.util.List.of() // authorities (지금은 비어도 됨)
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
