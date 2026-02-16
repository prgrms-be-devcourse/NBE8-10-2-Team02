package com.back.global.steam;

import com.back.global.steam.exception.SteamApiException;
import com.back.global.steam.exception.SteamRetryableException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class SteamRequestExecutor {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(429, 500, 502, 503);

    private final RestClient steamRestClient;
    private final SteamProperties props;

    @Retry(name = "steam")
    @RateLimiter(name = "steam")
    public <T> T execute(Function<UriBuilder, URI> uriFunction, Class<T> responseType, String actionName) {
        Objects.requireNonNull(uriFunction, "uriFunction must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        try {
            return steamRestClient.get()
                    .uri(uriBuilder -> uriFunction.apply(
                            uriBuilder.queryParam("key", props.apiKey())))
                    .retrieve()
                    .body(responseType);

        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            if (RETRYABLE_STATUS_CODES.contains(statusCode)) {
                log.warn("Steam {} 발생, 재시도 예정 - {}", statusCode, actionName);
                throw new SteamRetryableException(actionName, statusCode, e);
            }
            String msg = "%s 실패, status: %d, body: %s"
                    .formatted(actionName, statusCode, e.getResponseBodyAsString());
            throw new SteamApiException(msg, e);
        } catch (ResourceAccessException e) {
            log.warn("Steam 네트워크 오류, 재시도 예정 - {}, cause: {}", actionName, e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = "%s failed. error=%s".formatted(actionName, e.getMessage());
            throw new SteamApiException(msg, e);
        }
    }
}
