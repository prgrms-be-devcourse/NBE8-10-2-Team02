package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT MAX(g.lastFetchedAt) FROM Game g")
    Optional<Instant> findMaxLastFetchedAt();
}
