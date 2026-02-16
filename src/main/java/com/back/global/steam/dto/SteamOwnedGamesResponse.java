package com.back.global.steam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SteamOwnedGamesResponse(
        Response response
) {
    public record Response(
            @JsonProperty("game_count") int gameCount,
            List<SteamGameDto> games
    ) {
    }
}
