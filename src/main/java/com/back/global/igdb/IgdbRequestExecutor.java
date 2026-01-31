package com.back.global.igdb;

import com.back.global.exception.IgdbRetryableException;
import com.back.global.igdb.exception.IgdbApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbRequestExecutor {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(429, 500, 502, 503);

    private final RestClient igdbRestClient;
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;

    @Retryable(
            retryFor = {IgdbRetryableException.class, ResourceAccessException.class},
            maxAttempts = 4, //최초 1회 + 재시도 3회
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> T execute(String body, Class<T> responseType, String endPoint, String actionName) {
        Objects.requireNonNull(body, "IGDB request body must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        try {
            return igdbRestClient.post()
                    .uri(endPoint)
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", props.clientId())
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .body(body)
                    .retrieve()
                    .body(responseType);

        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            if (RETRYABLE_STATUS_CODES.contains(statusCode)) {
                log.warn("IGDB {} 발생, 재시도 예정 - {}", statusCode, actionName);
                throw new IgdbRetryableException(actionName, statusCode, e);
            }
            String msg = "%s 실패, status: %d, body: %s"
                    .formatted(actionName, statusCode, e.getResponseBodyAsString());
            throw new IgdbApiException(msg, e);
        } catch (ResourceAccessException e) {
            log.warn("IGDB 네트워크 오류, 재시도 예정 - {}, cause: {}", actionName, e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = "%s failed. error=%s".formatted(actionName, e.getMessage());
            throw new IgdbApiException(msg, e);
        }
    }
}
