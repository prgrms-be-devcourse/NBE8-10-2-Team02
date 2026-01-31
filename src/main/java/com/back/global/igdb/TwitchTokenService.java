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

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitchTokenService {

    private final RestClient twitchAuthRestClient;
    private final IgdbProperties props;

    private final AtomicReference<CachedToken> cache = new AtomicReference<>();

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
        // 만료 30초 전이면 재발급(여유 버퍼)
        TokenResponse res = fetchNewToken();
        Instant expiresAt = Instant.now().plusSeconds(res.expires_in());

        cache.set(new CachedToken(res.access_token(), expiresAt));
        return res.access_token();
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
