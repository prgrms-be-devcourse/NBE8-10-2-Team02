package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbGameDto(
        long id,
        String name,
        String summary,
        @JsonProperty("first_release_date")
        Long firstReleaseDateEpochSeconds
        // genres/platforms/keywords는 필요해지면 추가
) {}