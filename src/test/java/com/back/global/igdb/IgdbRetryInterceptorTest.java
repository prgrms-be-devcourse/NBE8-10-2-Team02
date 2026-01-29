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
import org.springframework.web.client.RestClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        IgdbRetryInterceptor interceptor = new IgdbRetryInterceptor(rateLimiter);

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
    @DisplayName("429 응답 후 재시도하여 성공")
    void retryOn429ThenSuccess() {
        server.enqueue(new MockResponse().setResponseCode(429));
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
        assertThat(server.getRequestCount()).isEqualTo(2);
        verify(rateLimiter, times(2)).acquire();
    }

    @Test
    @DisplayName("429 최대 재시도(3회) 초과 시 429 응답 반환")
    void maxRetryExceeded() {
        for (int i = 0; i < 4; i++) {
            server.enqueue(new MockResponse().setResponseCode(429));
        }

        assertThatThrownBy(() -> restClient.post()
                .uri("/test")
                .contentType(MediaType.TEXT_PLAIN)
                .body("test")
                .retrieve()
                .body(String.class))
                .isInstanceOf(RestClientResponseException.class);

        // 최초 1회 + 재시도 3회 = 4회
        assertThat(server.getRequestCount()).isEqualTo(4);
        verify(rateLimiter, times(4)).acquire();
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
    @DisplayName("200 응답은 재시도 없이 바로 반환")
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

    @Test
    @DisplayName("500 응답은 재시도하지 않음")
    void noRetryOn500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> restClient.post()
                .uri("/test")
                .contentType(MediaType.TEXT_PLAIN)
                .body("test")
                .retrieve()
                .body(String.class))
                .isInstanceOf(RestClientResponseException.class);

        assertThat(server.getRequestCount()).isEqualTo(1);
        verify(rateLimiter, times(1)).acquire();
    }
}
