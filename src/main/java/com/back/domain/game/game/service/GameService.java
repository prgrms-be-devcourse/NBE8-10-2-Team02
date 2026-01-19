package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameSearchRes;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final IgdbClient igdbClient;

    public List<GameSearchRes> search(String q) {
        return igdbClient.searchGames(q, 10).stream()
                .map(GameSearchRes::from).toList();
    }

    public IgdbGameDto getGame(long igdbId) {
        return igdbClient.getGame(igdbId);
    }

    /*private LocalDate toLocalDate(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }*/
}