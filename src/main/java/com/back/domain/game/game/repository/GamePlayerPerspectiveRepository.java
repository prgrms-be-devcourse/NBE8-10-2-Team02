package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GamePlayerPerspective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GamePlayerPerspectiveRepository extends JpaRepository<GamePlayerPerspective, Long> {

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM GamePlayerPerspective gpp WHERE gpp.game.id IN :gameIds")
    void deleteByGameIdIn(@Param("gameIds") Collection<Integer> gameIds);

    List<GamePlayerPerspective> findByGameId(int gameId);
}
