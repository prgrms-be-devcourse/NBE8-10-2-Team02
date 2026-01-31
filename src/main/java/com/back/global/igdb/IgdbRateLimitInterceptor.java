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
public class IgdbRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final RateLimiter igdbRateLimiter;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        igdbRateLimiter.acquire();
        log.debug("IGDB 요청: {} {}", request.getMethod(), request.getURI());
        return execution.execute(request, body);
    }
}
