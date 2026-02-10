package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByIgdbIdIn(Collection<Long> igdbIds);
}
