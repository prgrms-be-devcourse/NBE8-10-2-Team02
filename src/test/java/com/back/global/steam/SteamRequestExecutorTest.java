package com.back.global.steam;

import com.back.global.steam.exception.SteamApiException;
import com.back.global.steam.exception.SteamRetryableException;
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

public class SteamRequestExecutorTest {

    private MockWebServer server;
    private SteamRequestExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        RestClient restClient = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        SteamProperties props = mock(SteamProperties.class);
        when(props.apiKey()).thenReturn("test-api-key");

        executor = new SteamRequestExecutor(restClient, props);
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
                .setBody("{\"response\":{\"game_count\":1,\"games\":[{\"appid\":10}]}}"));

        String result = executor.execute(
                uriBuilder -> uriBuilder.path("/IPlayerService/GetOwnedGames/v1/").build(),
                String.class, "testAction");

        assertThat(result).contains("appid");
    }

    @Test
    @DisplayName("429 응답 시 SteamRetryableException 발생")
    void retryableOn429() {
        server.enqueue(new MockResponse().setResponseCode(429));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamRetryableException.class);
    }

    @Test
    @DisplayName("500 응답 시 SteamRetryableException 발생")
    void retryableOn500() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamRetryableException.class);
    }

    @Test
    @DisplayName("502 응답 시 SteamRetryableException 발생")
    void retryableOn502() {
        server.enqueue(new MockResponse().setResponseCode(502));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamRetryableException.class);
    }

    @Test
    @DisplayName("503 응답 시 SteamRetryableException 발생")
    void retryableOn503() {
        server.enqueue(new MockResponse().setResponseCode(503));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamRetryableException.class);
    }

    @Test
    @DisplayName("400 응답 시 SteamApiException 발생 (재시도 대상 아님)")
    void nonRetryableOn400() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamApiException.class)
                .hasMessageContaining("testAction");
    }

    @Test
    @DisplayName("403 응답 시 SteamApiException 발생 (잘못된 API 키)")
    void nonRetryableOn403() {
        server.enqueue(new MockResponse().setResponseCode(403).setBody("Forbidden"));

        assertThatThrownBy(() -> executor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(SteamApiException.class)
                .hasMessageContaining("403");
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

        SteamProperties props = mock(SteamProperties.class);
        when(props.apiKey()).thenReturn("test-api-key");

        SteamRequestExecutor timeoutExecutor = new SteamRequestExecutor(timeoutRestClient, props);

        server.enqueue(new MockResponse()
                .setBody("{}")
                .setHeadersDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> timeoutExecutor.execute(
                uriBuilder -> uriBuilder.path("/test").build(),
                String.class, "testAction"))
                .isInstanceOf(ResourceAccessException.class);
    }
}
