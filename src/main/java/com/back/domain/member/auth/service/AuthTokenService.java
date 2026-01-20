package com.back.domain.member.auth.service;

import com.back.domain.member.member.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secretKey}")
    private String secretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int expirationSeconds;

    public String genAccessToken(Member member) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * expirationSeconds);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder()
                .claim("id", member.getId())
                .claim("email", member.getEmail())
                .claim("nickname", member.getNickname())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Map<String, Object> payload(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Object payload = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parse(token)
                    .getPayload();

            return (Map<String, Object>) payload;
        } catch (Exception e) {
            return null;
        }
    }
}
