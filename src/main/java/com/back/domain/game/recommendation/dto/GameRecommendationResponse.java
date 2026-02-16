package com.back.domain.game.recommendation.dto;

public record GameRecommendationResponse(
        int gameId,
        String name,
        String coverImageId,
        double score,
        double similarity
) {
}
