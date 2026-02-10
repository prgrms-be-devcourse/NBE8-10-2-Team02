package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    List<Theme> findByIgdbIdIn(Collection<Long> igdbIds);
}
