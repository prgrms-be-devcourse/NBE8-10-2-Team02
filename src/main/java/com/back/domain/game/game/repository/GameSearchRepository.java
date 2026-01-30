package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSearchRepository extends JpaRepository<Game, Long>, GameSearchRepositoryCustom {
}
