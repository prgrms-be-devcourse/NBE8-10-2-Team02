package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbGenreDto(
        long id,
        String name
) {}
