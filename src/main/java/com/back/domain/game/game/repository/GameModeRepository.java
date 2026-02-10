package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GameModeRepository extends JpaRepository<GameMode, Long> {
    List<GameMode> findByIgdbIdIn(Collection<Long> igdbIds);
}
