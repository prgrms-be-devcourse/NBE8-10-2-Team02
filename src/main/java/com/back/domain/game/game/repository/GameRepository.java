package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findByIgdbId(Long igdbId);

    // 자체 서비스 인기 (조회수 기준)
    List<Game> findTop10ByOrderByViewCountDesc();

    // 자체 서비스 인기 (좋아요 기준)
    List<Game> findTop10ByOrderByLikeCountDesc();

    // 종합 인기 점수 기준 (JPQL)
    @Query("""                                                                                                   
      SELECT g FROM Game g
      WHERE g.coverImageId IS NOT NULL
      ORDER BY (
          COALESCE(g.igdbRating, 50.0) * 0.7 +
          LEAST(g.viewCount * 0.1 + g.likeCount * 2.0 + g.reviewCount * 5.0, 100.0) * 0.3
      ) DESC
      """)
    List<Game> findPopularGames(Pageable pageable);

    // IGDB rating 기준 (DB에 저장된 게임 중)
    List<Game> findTop10ByIgdbRatingNotNullOrderByIgdbRatingDesc();


    //bulk update
    @Modifying
    @Query("UPDATE Game g SET g.viewCount = g.viewCount + :delta WHERE g.igdbId = :igdbId")
    void incrementViewCount(@Param("igdbId") long igdbId, @Param("delta") long delta);
}
