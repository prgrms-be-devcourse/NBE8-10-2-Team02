package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GameKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GameKeywordRepository extends JpaRepository<GameKeyword, Long> {

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM GameKeyword gk WHERE gk.game.id IN :gameIds")
    void deleteByGameIdIn(@Param("gameIds") Collection<Integer> gameIds);

    List<GameKeyword> findByGameId(int gameId);

    @Query(value = "SELECT gk.keyword_id FROM game_keyword gk GROUP BY gk.keyword_id ORDER BY COUNT(*) DESC LIMIT 100",
            nativeQuery = true)
    List<Long> findTop100KeywordIdsByFrequency();
}
