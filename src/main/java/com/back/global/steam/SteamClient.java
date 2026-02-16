package com.back.global.steam;

import com.back.global.steam.dto.SteamGameDto;
import com.back.global.steam.dto.SteamOwnedGamesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SteamClient {
    //https://partner.steamgames.com/doc/webapi/IPlayerService#GetOwnedGames
    private static final String OWNED_GAMES_ENDPOINT = "/IPlayerService/GetOwnedGames/v1/";

    private final SteamRequestExecutor requestExecutor;

    public List<SteamGameDto> getOwnedGames(String steamId) {
        SteamOwnedGamesResponse response = requestExecutor.execute(
                uriBuilder -> uriBuilder
                        .path(OWNED_GAMES_ENDPOINT)
                        .queryParam("steamid", steamId)
                        .queryParam("include_appinfo", true)
                        .queryParam("include_played_free_games", true)
                        .queryParam("format", "json")
                        .build(),
                SteamOwnedGamesResponse.class,
                "getOwnedGames(steamId=%s)".formatted(steamId)
        );

        if (response == null || response.response() == null || response.response().games() == null) {
            return List.of();
        }
        return response.response().games();
    }
}
