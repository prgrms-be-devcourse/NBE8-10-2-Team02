package com.back.global.igdb;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_RETRIES = 3;

    private final RateLimiter igdbRateLimiter;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        int retryCount = 0;

        while (true) {
            igdbRateLimiter.acquire();
            log.debug("IGDB 요청: {} {}", request.getMethod(), request.getURI());

            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode().value() == 429 && retryCount < MAX_RETRIES) {
                retryCount++;
                long waitMs = (long) Math.pow(2, retryCount) * 500;
                log.warn("IGDB 429 발생, {}ms 후 재시도 ({}/{})", waitMs, retryCount, MAX_RETRIES);

                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("재시도 중 인터럽트 발생", e);
                }
                continue;
            }

            return response;
        }
    }
}
