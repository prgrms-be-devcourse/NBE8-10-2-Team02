package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IgdbGameDetailDto(
        long id,
        String name,
        String summary,
        @JsonProperty("first_release_date")
        Long firstReleaseDateEpochSeconds,
        IgdbCoverDto cover,
        List<IgdbGenreDto> genres,
        List<IgdbKeywordDto> keywords,
        List<IgdbPlatformDto> platforms
) {

}