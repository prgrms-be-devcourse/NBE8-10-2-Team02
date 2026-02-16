package com.back.domain.game.recommendation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GameVectorRepositoryImpl implements GameVectorRepositoryCustom {

    private static final int BATCH_SIZE = 50;
    private final EntityManager entityManager;

    @Override
    public void bulkUpdateFeatureVectors(Map<Integer, String> gameVectorMap) {
        if (gameVectorMap.isEmpty()) {
            return;
        }

        List<Map.Entry<Integer, String>> entries = new ArrayList<>(gameVectorMap.entrySet());

        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            List<Map.Entry<Integer, String>> batch = entries.subList(i, Math.min(i + BATCH_SIZE, entries.size()));

            StringBuilder sql = new StringBuilder(
                    "UPDATE game AS g SET feature_vector = cast(v.vec as vector) FROM (VALUES ");

            boolean first = true;
            for (Map.Entry<Integer, String> entry : batch) {
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
}
