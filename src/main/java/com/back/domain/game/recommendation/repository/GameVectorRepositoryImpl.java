package com.back.domain.game.recommendation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GameVectorRepositoryImpl implements GameVectorRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public void bulkUpdateFeatureVectors(Map<Integer, String> gameVectorMap) {
        if (gameVectorMap.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder(
                "UPDATE game AS g SET feature_vector = cast(v.vec as vector) FROM (VALUES ");

        boolean first = true;
        for (Map.Entry<Integer, String> entry : gameVectorMap.entrySet()) {
            if (!first) {
                sql.append(',');
            }
            sql.append('(').append(entry.getKey()).append(",'").append(entry.getValue()).append("')");
            first = false;
        }

        sql.append(") AS v(id, vec) WHERE g.id = v.id");

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }
}
