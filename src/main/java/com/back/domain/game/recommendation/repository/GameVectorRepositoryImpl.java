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
        if (gameVectorMap.isEmpty()) {
            return;
        }

        entityManager.unwrap(Session.class).doWork(connection -> {
            String sql = "UPDATE game SET feature_vector = cast(? as vector) WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int count = 0;
                for (Map.Entry<Integer, String> entry : gameVectorMap.entrySet()) {
                    ps.setString(1, entry.getValue());
                    ps.setInt(2, entry.getKey());
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
}
