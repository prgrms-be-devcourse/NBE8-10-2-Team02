package com.back.domain.game.game.dto;

import com.back.domain.game.game.entity.Game;
import com.back.global.igdb.dto.IgdbPopularGameDto;

public record PopularGameResponse(
        long igdbId,
        String name,
        String coverImageId,
        Double igdbRating,
        Integer igdbRatingCount,
        long viewCount,
        long likeCount,
        long reviewCount,
        double popularityScore
) {
    /**
     * IGDB 데이터만으로 생성 (DB에 없는 게임)
     * igdbPopularityValue: popularity_primitives에서 받은 정규화된 값 (0~100)
     */
    public static PopularGameResponse fromIgdb(IgdbPopularGameDto dto, double normalizedIgdbScore) {
        String coverId = dto.cover() != null ? dto.cover().imageId() : null;

        return new PopularGameResponse(
                dto.id(),
                dto.name(),
                coverId,
                dto.totalRating(),
                dto.totalRatingCount(),
                0, 0, 0,
                normalizedIgdbScore
        );
    }

    /**
     * DB 게임 + IGDB 인기도 정규화 값으로 생성
     */
    public static PopularGameResponse fromGame(Game game, double normalizedIgdbScore) {
        double serviceBonus = calculateServiceBonus(game);
        // IGDB 인기도(0~100) 80% + 자체 서비스 보너스(0~20) 20%
        double score = normalizedIgdbScore * 0.8 + serviceBonus * 0.2;

        return new PopularGameResponse(
                game.getIgdbId(),
                game.getName(),
                game.getCoverImageId(),
                game.getIgdbRating(),
                game.getIgdbRatingCount(),
                game.getViewCount(),
                game.getLikeCount(),
                game.getReviewCount(),
                Math.round(score * 10.0) / 10.0
        );
    }

    /**
     * DB 데이터만으로 생성 (자체 인기 순위용)
     */
    public static PopularGameResponse fromGame(Game game) {
        double serviceScore = calculateServiceBonus(game);

        return new PopularGameResponse(
                game.getIgdbId(),
                game.getName(),
                game.getCoverImageId(),
                game.getIgdbRating(),
                game.getIgdbRatingCount(),
                game.getViewCount(),
                game.getLikeCount(),
                game.getReviewCount(),
                Math.round(serviceScore * 10.0) / 10.0
        );
    }

    /**
     * 자체 서비스 보너스 점수 (0~100)
     */
    private static double calculateServiceBonus(Game game) {
        double score =
                game.getViewCount() * 0.5 +
                game.getLikeCount() * 3.0 +
                game.getReviewCount() * 5.0;

        return Math.min(score, 100.0);
    }
}
