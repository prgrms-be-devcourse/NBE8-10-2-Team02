package com.back.domain.game.recommendation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.util.Map;

@RequiredArgsConstructor
public class GameVectorRepositoryImpl implements GameVectorRepositoryCustom {

    private static final int BATCH_SIZE = 500;
    private final EntityManager entityManager;

    @Override
    public void bulkUpdateFeatureVectors(Map<Integer, String> gameVectorMap) {
        if (gameVectorMap.isEmpty()) return;

        entityManager.flush();

        entityManager.unwrap(Session.class).doWork(connection -> {
            // 1. TRUNCATE staging
            try (var stmt = connection.createStatement()) {
                stmt.execute("TRUNCATE game_vector_staging");
            }

            // 2. Batch INSERT into staging (reWriteBatchedInserts 활용)
            String insertSql = "INSERT INTO game_vector_staging (game_id, feature_vector) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                int count = 0;
                for (Map.Entry<Integer, String> entry : gameVectorMap.entrySet()) {
                    ps.setInt(1, entry.getKey());
                    ps.setString(2, entry.getValue());
                    ps.addBatch();

                    if (++count % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        ps.clearBatch();
                    }
                }
                ps.executeBatch();
            }

            // 3. UPDATE game JOIN staging
            try (var stmt = connection.createStatement()) {
                stmt.execute(
                        "UPDATE game SET feature_vector = cast(s.feature_vector as vector) " +
                        "FROM game_vector_staging s WHERE game.id = s.game_id"
                );
            }

            // 4. TRUNCATE staging
            try (var stmt = connection.createStatement()) {
                stmt.execute("TRUNCATE game_vector_staging");
            }
        });
    }
}
