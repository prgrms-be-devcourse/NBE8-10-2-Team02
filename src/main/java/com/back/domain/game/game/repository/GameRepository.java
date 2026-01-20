package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findByIgdbId(Long igdbId);
}
