package com.back.global.igdb;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitchTokenService {

    private final RestClient twitchAuthRestClient;
    private final IgdbProperties props;

    private final AtomicReference<CachedToken> cache = new AtomicReference<>();
    private final ReentrantLock tokenLock = new ReentrantLock();

    @PostConstruct
    public void warmUpToken() {
        try {
            getAccessToken();
        } catch (Exception e) {
            log.warn("Twitch token warm-up failed", e);
        }
    }

    public String getAccessToken() {
        CachedToken current = cache.get();

        // 만료 30초 이상이면 그냥 쓰고
        if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
            return current.token();
        }

        // 만료 30초 전이면 재발급 — 1개 스레드만 갱신, 나머지는 대기
        tokenLock.lock();
        try {
            // double-check: 대기 중 다른 스레드가 이미 갱신했을 수 있음
            current = cache.get();
            if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
                return current.token();
            }

            TokenResponse res = fetchNewToken();
            Instant expiresAt = Instant.now().plusSeconds(res.expires_in());
            cache.set(new CachedToken(res.access_token(), expiresAt));
            return res.access_token();
        } finally {
            tokenLock.unlock();
        }
    }

    private TokenResponse fetchNewToken() {
        // POST https://id.twitch.tv/oauth2/token
        // ?client_id=...&client_secret=...&grant_type=client_credentials
        URI uri = UriComponentsBuilder
                .fromUriString(props.tokenUrl())   // token-url은 반드시 https:// 로 시작
                .queryParam("client_id", props.clientId())
                .queryParam("client_secret", props.clientSecret())
                .queryParam("grant_type", "client_credentials")
                .build(true)
                .toUri();

        return twitchAuthRestClient.post()
                .uri(uri)
                .retrieve()
                .body(TokenResponse.class);
    }

    private record CachedToken(String token, Instant expiresAt) {}

    // Twitch OAuth 응답 주요 필드
    public record TokenResponse(
            String access_token,
            long expires_in,
            String token_type
    ) {}
}
