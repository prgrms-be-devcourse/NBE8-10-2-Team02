package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbVideoDto(
        long id,
        @JsonProperty("video_id")
        String videoId
) {}
