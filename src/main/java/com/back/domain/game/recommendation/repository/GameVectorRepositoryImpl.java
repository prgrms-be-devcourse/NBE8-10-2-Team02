package com.back.domain.game.recommendation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.util.Map;

@RequiredArgsConstructor
public class GameVectorRepositoryImpl implements GameVectorRepositoryCustom {

    private static final int BATCH_SIZE = 1000;
    private final EntityManager entityManager;

    /**
     * Chunk마다 호출: staging에만 UPSERT 한다.
     * (TRUNCATE/UPDATE JOIN은 여기서 하지 않음)
     */
    @Override
    public void bulkUpdateFeatureVectors(Map<Integer, String> gameVectorMap) {
        if (gameVectorMap.isEmpty()) return;

        entityManager.flush();

        entityManager.unwrap(Session.class).doWork(connection -> {
            String upsertSql =
                    "INSERT INTO game_vector_staging (game_id, feature_vector) " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT (game_id) DO UPDATE " +
                    "SET feature_vector = EXCLUDED.feature_vector";

            try (PreparedStatement ps = connection.prepareStatement(upsertSql)) {
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
        });
    }

    /**
     * Step 종료 시 1회 호출: staging 내용을 game으로 반영하고 staging 비움.
     */
    @Override
    public void applyStagingToGameAndTruncate() {
        entityManager.flush();

        entityManager.unwrap(Session.class).doWork(connection -> {
            try (var stmt = connection.createStatement()) {
                stmt.execute(
                        "UPDATE game g " +
                        "SET feature_vector = cast(s.feature_vector as vector) " +
                        "FROM game_vector_staging s " +
                        "WHERE g.id = s.game_id"
                );

                stmt.execute("TRUNCATE game_vector_staging");
            }
        });
    }
}
