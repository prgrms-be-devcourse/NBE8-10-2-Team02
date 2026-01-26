package com.back.global.igdb;


import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbInvolvedCompanyDto;
import com.back.global.igdb.dto.IgdbVideoDto;
import com.back.global.igdb.exception.IgdbApiException;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 요청 헤더: Client-ID, Authorization
 * body(APICALYPSE)가 원하는대로 들어갔는지 체크
 * IGDB 응답 JSON이 DTO로 잘 역직렬화되는지 체크
 */
public class IgdbClientTest {
    private IgdbProperties props = mock(IgdbProperties.class);
    private TwitchTokenService tokenService = mock(TwitchTokenService.class);
    private RateLimiter rateLimiter = mock(RateLimiter.class);

    private MockWebServer server;
    private IgdbClient igdbClient;

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

        igdbClient = new IgdbClient(restClient, props, tokenService, rateLimiter);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    @DisplayName("게임상세조회 - 성공")
    void t1_getGameDetail_success() throws Exception {
        String body = """
            [
                {
                    "id": 999,
                    "name": "Test Game",
                    "summary": "Hello",
                    "first_release_date": 1645747200,
                    "cover": {"id": 10, "image_id": "co4jni"},
                    "involved_companies": [
                        {
                            "id": 1,
                            "company": {"id": 100, "name": "FromSoftware"},
                            "developer": true,
                            "publisher": false
                        },
                        {
                            "id": 2,
                            "company": {"id": 200, "name": "Bandai Namco"},
                            "developer": false,
                            "publisher": true
                        }
                    ],
                    "genres": [
                        { "id": 1, "name": "RPG" },
                        { "id": 2, "name": "Action" }
                    ],
                    "platforms": [{"id": 6, "name": "PC (Microsoft Windows)"}]
                }
            ]
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        IgdbGameDetailDto detail = igdbClient.getGameDetail(999L);
        assertThat(detail).isNotNull();
        assertThat(detail.id()).isEqualTo(999L);
        assertThat(detail.name()).isEqualTo("Test Game");

        // cover.image_id
        assertThat(detail.cover()).isNotNull();
        assertThat(detail.cover().imageId()).isEqualTo("co4jni");

        //involved_companies
        assertThat(detail.involvedCompanies()).hasSize(2);
        // 개발사 검증
        assertThat(detail.involvedCompanies())
                .filteredOn(IgdbInvolvedCompanyDto::developer)
                .extracting(c -> c.company().name())
                .containsExactly("FromSoftware");
        // 퍼블리셔 검증
        assertThat(detail.involvedCompanies())
                .filteredOn(IgdbInvolvedCompanyDto::publisher)
                .extracting(c -> c.company().name())
                .containsExactly("Bandai Namco");

        // genres / platforms
        assertThat(detail.genres()).extracting("name")
                .containsExactlyInAnyOrder("RPG", "Action");
        assertThat(detail.platforms()).extracting("name")
                .contains("PC (Microsoft Windows)");

        RecordedRequest req = server.takeRequest();
        assertCommonRequest(req, "/games");

        String reqBody = req.getBody().readString(StandardCharsets.UTF_8);
        assertThat(reqBody).contains("fields");
        assertThat(reqBody).contains("cover.id,cover.image_id");
        assertThat(reqBody).contains("where id = 999;");
        assertThat(reqBody).contains("limit 1;");
    }

    @Test
    @DisplayName("게임상세조회 - 빈 배열 반환 시 null 리턴")
    void t2_getGameDetail_whenEmptyArray_returnsNull() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        IgdbGameDetailDto detail = igdbClient.getGameDetail(1L);
        assertThat(detail).isNull();
    }

    @Test
    @DisplayName("게임상세조회 - 500 예외")
    void t4_getGameDetail_500_throwsException() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThatThrownBy(() -> igdbClient.getGameDetail(1L))
                .isInstanceOf(IgdbApiException.class);
    }

    @Test
    @DisplayName("게임상세조회 - 429후 성공")
    void t5_getGameDetail_429_thenRetrySuccess() {
        String body = """
            [
                {
                    "id": 999,
                    "name": "Test Game",
                    "summary": "Hello",
                    "first_release_date": 1645747200,
                    "cover": {"id": 10, "image_id": "co4jni"},
                    "involved_companies": [
                        {
                            "id": 1,
                            "company": {"id": 100, "name": "FromSoftware"},
                            "developer": true,
                            "publisher": false
                        },
                        {
                            "id": 2,
                            "company": {"id": 200, "name": "Bandai Namco"},
                            "developer": false,
                            "publisher": true
                        }
                    ],
                    "genres": [
                        { "id": 1, "name": "RPG" },
                        { "id": 2, "name": "Action" }
                    ],
                    "platforms": [{"id": 6, "name": "PC (Microsoft Windows)"}]
                }
            ]
            """;

        server.enqueue(new MockResponse().setResponseCode(429));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                .setBody(body));

        IgdbGameDetailDto detail = igdbClient.getGameDetail(999L);

        assertThat(detail).isNotNull();
        assertThat(server.getRequestCount()).isEqualTo(2);
    }
    @Test
    @DisplayName("게임상세조회 - 429 최대 재시도(3회) 초과 시 예외 발생")
    void t6_getGameDetail_429_maxRetryExceeded() {
        for (int i = 0; i < 4; i++) {
            server.enqueue(new MockResponse().setResponseCode(429));
        }

        assertThatThrownBy(() -> igdbClient.getGameDetail(1L))
                .isInstanceOf(IgdbApiException.class);
    }
    
    @Test
    @DisplayName("영상Id조회 - 성공")
    public void t7_getVideoId_성공() throws Exception {
        String body = """
            [
                {
                    "id": 999,
                    "video_id": "VIDEO_ID"
                }
            ]
            """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        IgdbVideoDto igdbVideoDto = igdbClient.getVideoId(999L);
        assertThat(igdbVideoDto).isNotNull();
        assertThat(igdbVideoDto.id()).isEqualTo(999L);
        assertThat(igdbVideoDto.videoId()).isEqualTo("VIDEO_ID");

        RecordedRequest req = server.takeRequest();
        assertCommonRequest(req, "/game_videos");

        String reqBody = req.getBody().readString(StandardCharsets.UTF_8);
        assertThat(reqBody).contains("fields");
        assertThat(reqBody).contains("video_id;");
        assertThat(reqBody).contains("where game = 999;");
        assertThat(reqBody).contains("sort id desc;");
        assertThat(reqBody).contains("limit 1;");
    }

    @Test
    @DisplayName("영상Id조회 - 빈 배열 반환 시 null 리턴")
    void t8_getVideoId_whenEmptyArray_returnsNull() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        IgdbVideoDto igdbVideoDto = igdbClient.getVideoId(1L);
        assertThat(igdbVideoDto).isNull();
    }

    @Test
    @DisplayName("비슷한게임조회 - 성공")
    public void t9_getSimilarGameIds_success() throws Exception {
        String body = """
            [
                {
                    "id": 999,
                    "similar_games": [1,2,3,4,5]
                }
            ]
            """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        List<Long> similarGameIds = igdbClient.getSimilarGameIds(999L);
        assertThat(similarGameIds).containsExactly(1L, 2L, 3L, 4L, 5L);

        RecordedRequest req = server.takeRequest();
        assertCommonRequest(req, "/games");

        String reqBody = req.getBody().readString(StandardCharsets.UTF_8);
        assertThat(reqBody).contains("fields");
        assertThat(reqBody).contains("similar_games;");
        assertThat(reqBody).contains("where id = 999;");
        assertThat(reqBody).contains("limit 1;");
    }

    @Test
    @DisplayName("비슷한게임조회 - 빈 배열 반환 시 빈 배열 리턴")
    void t10_getSimilarGameIds_whenEmptyArray_returnsEmptyList() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[]"));

        List<Long> similarGameIds = igdbClient.getSimilarGameIds(1L);
        assertThat(similarGameIds).isEmpty();
    }

    @Test
    @DisplayName("postReq - 요청 헤더에 Client-ID와 Authorization이 포함")
    void t11_request_containsRequiredHeaders() throws Exception {
        String body = """
                [
                    {
                        "id": 1,
                        "name": "Test"
                    }
                ]
                """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        igdbClient.getGameDetail(1L);

        RecordedRequest req = server.takeRequest();

        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getHeader("Client-ID")).isEqualTo("test-client-id");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer test-access-token");
        assertThat(req.getHeader("Content-Type")).startsWith("text/plain");
    }

    @Test
    @DisplayName("모든 요청마다 RateLimiter.acquire()가 호출되어 rate limit 방지")
    void t12_rateLimiter_acquireCalledOnEveryRequest() {
        String body = """
            [{"id": 1, "name": "Test"}]
            """;

        int requestCount = 5;
        for (int i = 0; i < requestCount; i++) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(body));
        }

        for (int i = 0; i < requestCount; i++) {
            igdbClient.getGameDetail(1L);
        }

        verify(rateLimiter, times(requestCount)).acquire();
    }

    // 공통 요청 검증
    private void assertCommonRequest(RecordedRequest req, String path) {
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo(path);

        assertThat(req.getHeader("Client-ID")).isEqualTo("test-client-id");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer test-access-token");

        assertThat(req.getHeader("Content-Type")).startsWith("text/plain");
    }
}
