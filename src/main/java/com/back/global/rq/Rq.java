package com.back.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public String getHeader(String name, String defaultValue) {
        return Optional.ofNullable(req.getHeader(name))
                .filter(v -> !v.isBlank())
                .orElse(defaultValue);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies ->
                        Arrays.stream(cookies)
                                .filter(c -> c.getName().equals(name))
                                .map(Cookie::getValue)
                                .filter(v -> !v.isBlank())
                                .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        cookie.setSecure(false);
        cookie.setMaxAge(value.isBlank() ? 0 : 60 * 60 * 24 * 365);

        resp.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, "");
    }
}
