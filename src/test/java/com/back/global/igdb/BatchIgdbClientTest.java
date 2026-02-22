package com.back.global.igdb;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BatchIgdbClient 단위 테스트
 * - fetchGamePage 요청 body (APICALYPSE) 검증
 * - updatedAfterEpoch 유무에 따른 where절 포함 여부 검증
 */
public class BatchIgdbClientTest {

    private IgdbProperties props = mock(IgdbProperties.class);
    private TwitchTokenService tokenService = mock(TwitchTokenService.class);

    private MockWebServer server;
    private BatchIgdbClient batchIgdbClient;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        String baseUrl = server.url("/").toString();
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        when(props.clientId()).thenReturn("test-client-id");
        when(tokenService.getAccessToken()).thenReturn("test-access-token");

        BatchIgdbRequestExecutor requestExecutor = new BatchIgdbRequestExecutor(restClient, props, tokenService);
        batchIgdbClient = new BatchIgdbClient(requestExecutor);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    @DisplayName("fetchGamePage - updatedAfterEpoch 없으면 where절 없이 요청")
    void t1_fetchGamePage_withoutFilter() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        batchIgdbClient.fetchGamePage(0, 500, null);

        RecordedRequest req = server.takeRequest();
        String reqBody = req.getBody().readString(StandardCharsets.UTF_8);
        assertThat(reqBody).doesNotContain("where updated_at");
        assertThat(reqBody).contains("offset 0;");
        assertThat(reqBody).contains("limit 500;");
    }

    @Test
    @DisplayName("fetchGamePage - updatedAfterEpoch 있으면 where updated_at 조건 포함")
    void t2_fetchGamePage_withFilter() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        batchIgdbClient.fetchGamePage(0, 500, 1700000000L);

        RecordedRequest req = server.takeRequest();
        String reqBody = req.getBody().readString(StandardCharsets.UTF_8);
        assertThat(reqBody).contains("where updated_at > 1700000000;");
        assertThat(reqBody).contains("offset 0;");
        assertThat(reqBody).contains("limit 500;");
    }

    @Test
    @DisplayName("fetchGamePage - 요청 헤더에 Client-ID와 Authorization이 포함")
    void t3_fetchGamePage_containsRequiredHeaders() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        batchIgdbClient.fetchGamePage(0, 500, null);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getHeader("Client-ID")).isEqualTo("test-client-id");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer test-access-token");
        assertThat(req.getHeader("Content-Type")).startsWith("text/plain");
    }
}
