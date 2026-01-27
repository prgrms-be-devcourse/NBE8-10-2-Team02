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
    public static PopularGameResponse fromIgdb(IgdbPopularGameDto dto) {
        String coverId = dto.cover() != null ? dto.cover().imageId() : null;
        double score = dto.totalRating() != null ? dto.totalRating() : 0.0;

        return new PopularGameResponse(
                dto.id(),
                dto.name(),
                coverId,
                dto.totalRating(),
                dto.totalRatingCount(),
                0, 0, 0,  // 자체 데이터 없음
                score
        );
    }

    public static PopularGameResponse fromGame(Game game) {
        double score = calculateScore(game);

        return new PopularGameResponse(
                game.getIgdbId(),
                game.getName(),
                game.getCoverImageId(),
                game.getIgdbRating(),
                game.getIgdbRatingCount(),
                game.getViewCount(),
                game.getLikeCount(),
                game.getReviewCount(),
                score
        );
    }

    // 종합 인기 점수 계산
    private static double calculateScore(Game game) {
        // IGDB 점수 (0~100), 없으면 50
        double igdbScore = game.getIgdbRating() != null ? game.getIgdbRating() : 50.0;

        // 자체 서비스 점수 계산
        double serviceScore =
                game.getViewCount() * 0.1 +                 // 조회 1회 = 0.1점
                        game.getLikeCount() * 2.0 +         // 좋아요 1개 = 2점
                        game.getReviewCount() * 5.0;        // 리뷰 1개 = 5점

        // 서비스 점수 상한 (100점)
        serviceScore = Math.min(serviceScore, 100.0);

        // 혼합 (IGDB 70% + 자체 30%)
        return (igdbScore * 0.7) + (serviceScore * 0.3);
    }
}