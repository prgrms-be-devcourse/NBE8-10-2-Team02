package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GameGameMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GameGameModeRepository extends JpaRepository<GameGameMode, Long> {

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM GameGameMode ggm WHERE ggm.game.id IN :gameIds")
    void deleteByGameIdIn(@Param("gameIds") Collection<Integer> gameIds);

    List<GameGameMode> findByGameId(int gameId);
}
