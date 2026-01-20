package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")) return true;

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
            writeError(response, e.getRsData());
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        }

        String[] bits = authorization.split(" ");
        String apiKey = "";
        String accessToken = "";

        if (bits.length == 2) {
            accessToken = bits[1].trim();
        } else if (bits.length >= 3) {
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

        if (apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Member member = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));

        setAuthentication(member.getId(), member.getEmail(), member.getNickname());

        String newAccessToken = memberService.genAccessToken(member);
        response.setHeader("Authorization", "Bearer " + member.getApiKey() + " " + newAccessToken);

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(int id, String email, String nickname) {
        List<SimpleGrantedAuthority> authorities = List.of();

        SecurityUser user = new SecurityUser(id, email, nickname, "", authorities);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeError(HttpServletResponse response, RsData<Void> rsData) throws IOException {
        response.setStatus(rsData.statusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(rsData));
    }
}
