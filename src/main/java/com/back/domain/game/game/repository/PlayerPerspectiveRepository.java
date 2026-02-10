package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.PlayerPerspective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PlayerPerspectiveRepository extends JpaRepository<PlayerPerspective, Long> {
    List<PlayerPerspective> findByIgdbIdIn(Collection<Long> igdbIds);
}
