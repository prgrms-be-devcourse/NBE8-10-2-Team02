package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbCoverDto(
        long id,
        @JsonProperty("image_id")
        String imageId
) {}