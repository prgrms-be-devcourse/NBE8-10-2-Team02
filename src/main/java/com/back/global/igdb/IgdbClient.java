package com.back.global.igdb;

import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.exception.IgdbApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * IgdbClient: HTTP 호출만 담당
 * 엔드포인트, 헤더, 바디(query), status code 처리
 * 응답을 DTO로 역직렬화
 */
@Component
@RequiredArgsConstructor
public class IgdbClient {

    private final RestClient igdbRestClient;
    private final IgdbProperties props;
    private final TwitchTokenService tokenService;


    // 예: 게임 검색
    public List<IgdbGameSummaryDto> searchGames(String keyword, int limit) {
        // IGDB Query Language (APICALYPSE)
        // search "elden ring"; fields id,name,summary,first_release_date,cover.url; limit 10;
        String body = """
                search "%s";
                fields id,name,summary,first_release_date;
                limit %d;
                """.formatted(escape(keyword), limit);

        try {
            IgdbGameSummaryDto[] res = igdbRestClient.post()
                    .uri("/games")
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", props.clientId())
                    .header("Authorization", "Bearer " + tokenService.getAccessToken())
                    .body(body)
                    .retrieve() // 요청을 보내고 응답을 가져올 준비를 하는 단계, 응답(상태코드, 헤더, 바디)을 받을 수 있는 핸들러가 만들어짐
                    .body(IgdbGameSummaryDto[].class); // 응답 body를 어떤 타입으로 변환해서 꺼낼지 정하는 것 IGDB가 JSON 배열을 준다 -> Jackson이 IgdbGameDto[]로 역직렬화 해줌

            return res == null ? List.of() : List.of(res);

        } catch (RestClientResponseException e) {
            // 나중에 로깅 + 도메인 예외로 변환
            throw new IgdbApiException("IGDB searchGames failed: " + e.getResponseBodyAsString(), e);
        }
    }

    // 게임 상세 가져오기(id로)
    public IgdbGameDetailDto getGameDetail(long igdbId) {
        String body = """
            fields
                id,name,summary,first_release_date,
                genres.id,genres.name,
                platforms.id,platforms.name,
                keywords.id,keywords.name,
                cover.id,cover.image_id;
            where id = %d;
            limit 1;
        """.formatted(igdbId);

        IgdbGameDetailDto res = igdbRestClient.post()
                .uri("/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID", props.clientId())
                .header("Authorization", "Bearer " + tokenService.getAccessToken())
                .body(body)
                .retrieve()
                .body(IgdbGameDetailDto.class);

        return res;
    }

    private String escape(String s) {
        // 검색어에 " 들어갈 수 있으니 이스케이프
        return s.replace("\"", "\\\"");
    }
}
