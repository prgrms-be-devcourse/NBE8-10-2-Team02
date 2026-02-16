package com.back.global.steam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamGameDto(
        @JsonProperty("appid") long appId,
        String name,
        @JsonProperty("playtime_forever") int playtimeForever,
        @JsonProperty("img_icon_url") String imgIconUrl
) {
}
