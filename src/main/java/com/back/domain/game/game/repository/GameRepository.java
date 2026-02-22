package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    List<Game> findByIgdbIdIn(Collection<Long> igdbIds);

    Optional<Game> findByIgdbId(Long igdbId);

    // 자체 서비스 인기 (조회수 기준)
    List<Game> findTop10ByOrderByViewCountDesc();

    // 자체 서비스 인기 (좋아요 기준)
    List<Game> findTop10ByOrderByLikeCountDesc();

    //bulk update
    @Modifying
    @Query("UPDATE Game g SET g.viewCount = g.viewCount + :delta WHERE g.igdbId = :igdbId")
    void incrementViewCount(@Param("igdbId") long igdbId, @Param("delta") long delta);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Game g SET g.likeCount = g.likeCount + 1 WHERE g.id = :gameId")
    void incrementLikeCount(@Param("gameId") int gameId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Game g SET g.likeCount = g.likeCount - 1 WHERE g.id = :gameId AND g.likeCount > 0")
    void decrementLikeCount(@Param("gameId") int gameId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Game g SET g.reviewCount = g.reviewCount + 1 WHERE g.id = :gameId")
    void incrementReviewCount(@Param("gameId") int gameId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Game g SET g.reviewCount = g.reviewCount - 1 WHERE g.id = :gameId AND g.reviewCount > 0")
    void decrementReviewCount(@Param("gameId") int gameId);

    @Query("SELECT MAX(g.lastFetchedAt) FROM Game g")
    Optional<Instant> findMaxLastFetchedAt();

    // IGDB fallback: 이름 ILIKE 검색 (Circuit Breaker fallback 용)
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY g.likeCount DESC")
    List<Game> findByNameContainingIgnoreCaseLimited(@Param("name") String name, Pageable pageable);

    // IGDB fallback: 좋아요 기준 인기 게임 (Circuit Breaker fallback 용)
    List<Game> findByOrderByLikeCountDesc(Pageable pageable);
}
