package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PopularityPrimitiveRow(
        long id,
        @JsonProperty("game_id") long gameId,
        BigDecimal value,
        @JsonProperty("popularity_type") int popularity_type
) {}