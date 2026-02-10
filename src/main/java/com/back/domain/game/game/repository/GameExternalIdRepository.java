package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.GameExternalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GameExternalIdRepository extends JpaRepository<GameExternalId, Long> {
    List<GameExternalId> findByPlatformAndExternalIdIn(String platform, Collection<String> externalIds);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM GameExternalId gei WHERE gei.game.id IN :gameIds")
    void deleteByGameIdIn(@Param("gameIds") Collection<Integer> gameIds);
}
