package com.back.domain.game.recommendation.repository;

import java.util.Map;

public interface GameVectorRepositoryCustom {

    void bulkUpdateFeatureVectors(Map<Integer, String> gameVectorMap);

    void applyStagingToGameAndTruncate();
}
