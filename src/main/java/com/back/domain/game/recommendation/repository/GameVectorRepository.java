package com.back.domain.game.recommendation.repository;

import com.back.domain.game.game.entity.Game;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameVectorRepository extends JpaRepository<Game, Integer>, GameVectorRepositoryCustom {

    @Query(value = "SELECT g.id FROM game g WHERE g.feature_vector IS NULL",
            nativeQuery = true)
    List<Integer> findGameIdsWithoutVector(Pageable pageable);

    @Query(value = """
            SELECT g.id, g.feature_vector::text
            FROM member_game mg JOIN game g ON mg.game_id = g.id
            WHERE mg.member_id = :memberId AND g.feature_vector IS NOT NULL
            """,
            nativeQuery = true)
    List<Object[]> findFeatureVectorsByMemberId(@Param("memberId") int memberId);

    @Query(value = """
            SELECT g.id, g.name, g.cover_image_id, g.aggregated_rating, g.like_count,
                   1 - (g.feature_vector <=> cast(:profileVector as vector)) AS similarity
            FROM game g
            WHERE g.feature_vector IS NOT NULL
              AND g.id NOT IN (:excludeGameIds)
            ORDER BY g.feature_vector <=> cast(:profileVector as vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Object[]> findSimilarGamesByUserVector(
            @Param("profileVector") String profileVector,
            @Param("excludeGameIds") List<Integer> excludeGameIds,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT g.igdb_id, g.name, g.cover_image_id, g.aggregated_rating, g.like_count,
                   1 - (g.feature_vector <=> target.feature_vector) AS similarity
            FROM game g, (SELECT feature_vector FROM game WHERE igdb_id = :igdbId) target
            WHERE g.feature_vector IS NOT NULL
              AND g.igdb_id != :igdbId
            ORDER BY g.feature_vector <=> target.feature_vector
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Object[]> findSimilarGamesByIgdbId(
            @Param("igdbId") long igdbId,
            @Param("limit") int limit
    );
}
