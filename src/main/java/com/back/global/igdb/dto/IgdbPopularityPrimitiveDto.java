package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbPopularityPrimitiveDto(
        long id,
        @JsonProperty("game_id") long gameId,
        double value,
        @JsonProperty("popularity_type") int popularityType
) {}