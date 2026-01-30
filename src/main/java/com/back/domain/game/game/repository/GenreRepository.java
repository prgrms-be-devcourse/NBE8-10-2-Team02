package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
//    Optional<Genre> findByIgdbId(long igdbId);
    List<Genre> findByIgdbIdIn(Collection<Long> igdbIds);
}
