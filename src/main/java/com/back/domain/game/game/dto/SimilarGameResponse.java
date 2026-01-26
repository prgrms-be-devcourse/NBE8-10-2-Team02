package com.back.domain.game.game.dto;

public record SimilarGameResponse(
        long igdbId,
        String name,
        String coverImageId
) {}
