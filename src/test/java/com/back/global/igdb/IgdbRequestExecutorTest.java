package com.back.global.igdb;

import com.back.global.exception.IgdbRetryableException;
import com.back.global.igdb.exception.IgdbApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IgdbRequestExecutorTest {

    private MockWebServer server;
    private IgdbRequestExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        RestClient restClient = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        IgdbProperties props = mock(IgdbProperties.class);
        when(props.clientId()).thenReturn("test-client-id");

        TwitchTokenService tokenService = mock(TwitchTokenService.class);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        executor = new IgdbRequestExecutor(restClient, props, tokenService);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    @DisplayName("200 응답 시 정상 반환")
    void successOn200() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[{\"id\": 1}]"));

        String result = executor.execute("test body", String.class, "/games", "testAction");

        assertThat(result).contains("id");
    }

    @Test
    @DisplayName("429 응답 시 IgdbRetryableException 발생")
    void retryableOn429() {
        server.enqueue(new MockResponse().setResponseCode(429));

        assertThatThrownBy(() -> executor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(IgdbRetryableException.class);
    }

    @Test
    @DisplayName("500 응답 시 IgdbRetryableException 발생")
    void retryableOn500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> executor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(IgdbRetryableException.class);
    }

    @Test
    @DisplayName("502 응답 시 IgdbRetryableException 발생")
    void retryableOn502() {
        server.enqueue(new MockResponse().setResponseCode(502));

        assertThatThrownBy(() -> executor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(IgdbRetryableException.class);
    }

    @Test
    @DisplayName("503 응답 시 IgdbRetryableException 발생")
    void retryableOn503() {
        server.enqueue(new MockResponse().setResponseCode(503));

        assertThatThrownBy(() -> executor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(IgdbRetryableException.class);
    }

    @Test
    @DisplayName("400 응답 시 IgdbApiException 발생 (재시도 대상 아님)")
    void nonRetryableOn400() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

        assertThatThrownBy(() -> executor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(IgdbApiException.class)
                .hasMessageContaining("testAction");
    }

    @Test
    @DisplayName("네트워크 타임아웃 시 ResourceAccessException 발생")
    void timeoutThrowsResourceAccessException() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(1));

        RestClient timeoutRestClient = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestFactory(factory)
                .build();

        IgdbProperties props = mock(IgdbProperties.class);
        when(props.clientId()).thenReturn("test-client-id");
        TwitchTokenService tokenService = mock(TwitchTokenService.class);
        when(tokenService.getAccessToken()).thenReturn("test-token");

        IgdbRequestExecutor timeoutExecutor = new IgdbRequestExecutor(timeoutRestClient, props, tokenService);

        server.enqueue(new MockResponse()
                .setBody("{}")
                .setHeadersDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> timeoutExecutor.execute("test body", String.class, "/games", "testAction"))
                .isInstanceOf(ResourceAccessException.class);
    }
}
