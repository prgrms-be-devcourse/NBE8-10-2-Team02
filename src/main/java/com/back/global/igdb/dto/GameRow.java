package com.back.global.igdb.dto;

import java.util.List;

public record GameRow(
        long id,
        String name,
        IgdbCoverDto cover,
        List<IgdbGenreDto> genres
    ) {}