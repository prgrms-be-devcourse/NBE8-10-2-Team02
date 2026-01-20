package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbGameSummaryDto(
        long id,
        String name,
        String summary,
        @JsonProperty("first_release_date")
        Long firstReleaseDateEpochSeconds
) {}