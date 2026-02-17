package com.back.global.batch.tasklet;

public final class VectorBatchSql {

    private VectorBatchSql() {}

    public static final String VECTOR_INDEX_NAME = "ix_game_feature_vector";

    public static final String DROP_VECTOR_INDEX =
            "DROP INDEX IF EXISTS " + VECTOR_INDEX_NAME;

    public static final String CREATE_VECTOR_INDEX =
            "CREATE INDEX IF NOT EXISTS " + VECTOR_INDEX_NAME +
            " ON game USING hnsw (feature_vector vector_cosine_ops)";

    public static final String TRUNCATE_VECTOR_STAGING =
            "TRUNCATE game_vector_staging";

    public static final String APPLY_STAGING_TO_GAME =
            "UPDATE game g " +
            "SET feature_vector = cast(s.feature_vector as vector) " +
            "FROM game_vector_staging s " +
            "WHERE g.id = s.game_id";
}
