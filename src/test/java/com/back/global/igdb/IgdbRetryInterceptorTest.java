package com.back.global.igdb;

import com.google.common.util.concurrent.RateLimiter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IgdbRetryInterceptorTest {

    private MockWebServer server;
    private RestClient restClient;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        rateLimiter = mock(RateLimiter.class);
        IgdbRateLimitInterceptor interceptor = new IgdbRateLimitInterceptor(rateLimiter);

        restClient = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestInterceptor(interceptor)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    @DisplayName("모든 요청마다 RateLimiter.acquire() 호출")
    void rateLimiterCalledOnEveryRequest() {
        int requestCount = 3;
        for (int i = 0; i < requestCount; i++) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{}"));
        }

        for (int i = 0; i < requestCount; i++) {
            restClient.post()
                    .uri("/test")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("test")
                    .retrieve()
                    .body(String.class);
        }

        verify(rateLimiter, times(requestCount)).acquire();
    }

    @Test
    @DisplayName("200 응답은 바로 반환")
    void noRetryOn200() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[{\"id\": 1}]"));

        String result = restClient.post()
                .uri("/test")
                .contentType(MediaType.TEXT_PLAIN)
                .body("test")
                .retrieve()
                .body(String.class);

        assertThat(result).contains("id");
        assertThat(server.getRequestCount()).isEqualTo(1);
        verify(rateLimiter, times(1)).acquire();
    }
}
