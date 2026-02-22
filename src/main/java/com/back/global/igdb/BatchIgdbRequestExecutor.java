package com.back.global.igdb;

import com.back.global.exception.IgdbRetryableException;
import com.back.global.igdb.exception.IgdbApiException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;
import java.util.Set;

/**
 * 배치 전용 IGDB Request Executor.
 *
 * - batchIgdbRestClient 사용 (connect 5s / read 30s)
 *   → 500건 대용량 쿼리도 안정적으로 처리
 * - @RateLimiter(name="igdb-batch"): 배치 전용 rate limiter (timeout 5s)
 *   → 사용자 경로(200ms 즉시 포기)와 분리. 배치는 슬롯 날 때까지 5s까지 대기 허용
 * - @Retry(name="igdb-batch"): 배치 전용 재시도 설정 (4회, exponential backoff)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchIgdbRequestExecutor {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(429, 500, 502, 503);

    private final RestClient batchIgdbRestClient;  // bean name으로 auto-wire
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;

    @Retry(name = "igdb-batch")
    @RateLimiter(name = "igdb-batch")
    public <T> T execute(String body, Class<T> responseType, String endPoint, String actionName) {
        Objects.requireNonNull(body, "IGDB request body must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        try {
            return batchIgdbRestClient.post()
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
                log.warn("IGDB(batch) {} 발생, 재시도 예정 - {}", statusCode, actionName);
                throw new IgdbRetryableException(actionName, statusCode, e);
            }
            String msg = "%s 실패, status: %d, body: %s"
                    .formatted(actionName, statusCode, e.getResponseBodyAsString());
            throw new IgdbApiException(msg, e);
        } catch (ResourceAccessException e) {
            log.warn("IGDB(batch) 네트워크 오류, 재시도 예정 - {}, cause: {}", actionName, e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = "%s failed. error=%s".formatted(actionName, e.getMessage());
            throw new IgdbApiException(msg, e);
        }
    }
}
