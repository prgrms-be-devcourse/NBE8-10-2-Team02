package com.back.global.igdb.service;

import com.back.domain.game.game.dto.GameSearchCondition;
import com.back.global.igdb.IgdbCircuitBreakerClient;
import com.back.global.igdb.IgdbProperties;
import com.back.global.igdb.TwitchTokenService;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.dto.IgdbPlatformDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class IgdbService {

    private final RestTemplate restTemplate;
    private final TwitchTokenService twitchTokenService;
    private final IgdbProperties props;
    private final IgdbCircuitBreakerClient igdbClient;

    public List<IgdbGameSummaryDto> search(GameSearchCondition condition) {

        String query = condition.getQuery();
        List<Long> genreIds = condition.getGenreIds();
        List<Long> platformIds = condition.getPlatformIgdbIds();

        int size = condition.getSize() != null ? condition.getSize() : 20;
        int page = condition.getPage() != null ? condition.getPage() : 1;
        int offset = (page - 1) * size;

//        검색결과 없으면 종료
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // IGDB 쿼리는 search + genre만 담당
        String body = buildIgdbQuery(query, genreIds, platformIds, size, offset);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", props.clientId());
        headers.setBearerAuth(twitchTokenService.getAccessToken());
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

//        API 호출
        try {
            ResponseEntity<IgdbGameSummaryDto[]> response =
                    restTemplate.postForEntity(
                            "https://api.igdb.com/v4/games",
                            request,
                            IgdbGameSummaryDto[].class
                    );

            List<IgdbGameSummaryDto> results =
                    Arrays.asList(response.getBody());

            // 서버에서 name AND 조건
            return results.stream()
                    .filter(d ->
                            d.name() != null &&
                                    d.name().toLowerCase().contains(query.toLowerCase())
                    )
                    .toList();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("IGDB error (status={})", e.getStatusCode(), e);
            return List.of();
        }
    }

    /**
     * IGDB 쿼리 생성 책임 분리
     */
    private String buildIgdbQuery(String query, List<Long> genreIds, List<Long> platformIds, int size, int offset) {
        StringBuilder sb = new StringBuilder();

        // 'search' 대신 'fields'를 먼저 선언
        sb.append("fields id, name, first_release_date, cover.image_id, genres, platforms;\n");

        // 모든 조건을 where 절로 통합
        List<String> whereConditions = new ArrayList<>();

        // 1. 이름 검색 (Case-insensitive matching)
        if (query != null && !query.isBlank()) {
            // * 기호를 써서 와일드카드 검색 (contains 효과)
            whereConditions.add("name ~ *\"" + query + "\"*");
        }

        // 2. 장르 필터
        if (genreIds != null && !genreIds.isEmpty()) {
            String genreCondition = genreIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            whereConditions.add("genres = (" + genreCondition + ")");
        }

        // 3. 플랫폼 필터
        if (platformIds != null && !platformIds.isEmpty()) {
            String platformCondition = platformIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            whereConditions.add("platforms = (" + platformCondition + ")");
        }

        if (!whereConditions.isEmpty()) {
            sb.append("where ");
            sb.append(String.join(" & ", whereConditions));
            sb.append(";\n");
        }

        // 정렬 추가
        sb.append("sort first_release_date desc;\n");

        // 페이지네이션
        sb.append("limit ").append(size).append("; ");
        sb.append("offset ").append(offset).append(";");

        return sb.toString();
    }

    public List<IgdbGenreDto> getGenres() {
        return igdbClient.fetchGenres();
    }
    public Map<Long, String> getPlatformNameMap(Set<Long> platformIds) {
        if (platformIds == null || platformIds.isEmpty()) {
            return Map.of();
        }

        String platformCondition = platformIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String body = """
        fields id,name;
        where id = (%s);
    """.formatted(platformCondition);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", props.clientId());
        headers.setBearerAuth(twitchTokenService.getAccessToken());
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<IgdbPlatformDto[]> response =
                    restTemplate.postForEntity(
                            "https://api.igdb.com/v4/platforms",
                            request,
                            IgdbPlatformDto[].class
                    );

            return Arrays.stream(response.getBody())
                    .collect(Collectors.toMap(
                            IgdbPlatformDto::id,
                            IgdbPlatformDto::name
                    ));

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("IGDB platform error", e);
            return Map.of();
        }
    }


}
