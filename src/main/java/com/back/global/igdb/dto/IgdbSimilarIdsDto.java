package com.back.global.igdb.dto;

import java.util.List;

public record IgdbSimilarIdsDto(
        long id,
        List<Long> similar_games
) { }