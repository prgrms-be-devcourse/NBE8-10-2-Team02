package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GameKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface GameKeywordRepository extends JpaRepository<GameKeyword, Long> {

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM GameKeyword gk WHERE gk.game.id IN :gameIds")
    void deleteByGameIdIn(@Param("gameIds") Collection<Integer> gameIds);
}
