package com.back.global.igdb.service;

import com.back.domain.game.game.dto.GameSearchCondition;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.IgdbProperties;
import com.back.global.igdb.TwitchTokenService;
import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IgdbService {

    private final RestTemplate restTemplate;
    private final TwitchTokenService twitchTokenService;
    private final IgdbProperties props;
    private final IgdbClient igdbClient;

    public List<IgdbGameSummaryDto> search(GameSearchCondition condition) {

        String query = condition.getQuery();

        if (query == null || query.isBlank()) {
            return List.of();
        }

        String body = """
            search "%s";
            fields id,name,first_release_date,cover.image_id,genres;
            limit 10;
        """.formatted(query);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", props.clientId());
        headers.setBearerAuth(twitchTokenService.getAccessToken());
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<IgdbGameSummaryDto[]> response =
                    restTemplate.postForEntity(
                            "https://api.igdb.com/v4/games",
                            request,
                            IgdbGameSummaryDto[].class
                    );

            return Arrays.asList(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(" IGDB error (status={})", e.getStatusCode(), e);
            return List.of();
        }
    }

    public List<IgdbGenreDto> getGenres() {
        return igdbClient.fetchGenres();
    }
}
