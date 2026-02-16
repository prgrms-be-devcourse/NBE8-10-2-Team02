package com.back.domain.game.recommendation.service;

import com.back.domain.game.game.entity.*;
import com.back.global.vector.VectorDimensionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.back.global.vector.VectorDimensionMapper.*;
import static org.aspectj.weaver.Advice.countOnes;
import static org.assertj.core.api.Assertions.assertThat;

class GameVectorServiceTest {

    private VectorDimensionMapper mapper;
    private GameVectorService service;

    @BeforeEach
    void setUp() {
        mapper = new VectorDimensionMapper();
        mapper.refresh(
                Map.of(1L, 0, 2L, 1, 3L, 5),       // genre: id→index
                Map.of(10L, 0, 11L, 3),              // theme
                Map.of(100L, 0, 101L, 50, 102L, 99), // keyword
                Map.of(200L, 0, 201L, 2),             // gameMode
                Map.of(300L, 0, 301L, 4)              // perspective
        );
        service = new GameVectorService(mapper);
    }

    @Nested
    @DisplayName("buildFeatureVector")
    class BuildFeatureVector {

        @Test
        @DisplayName("모든 속성이 비어있으면 전부 0.0인 벡터를 반환한다")
        void allEmpty_returnsZeroVector() {
            float[] vector = service.buildFeatureVector(
                    List.of(), List.of(), List.of(), List.of(), List.of()
            );

            assertThat(vector).hasSize(TOTAL_DIMENSIONS);
            assertThat(vector).containsOnly(0.0f);
        }

        @Test
        @DisplayName("장르 2개 → 해당 오프셋+인덱스 위치만 1.0")
        void genres_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(genre(1L), genre(2L)),
                    List.of(), List.of(), List.of(), List.of()
            );

            assertThat(vector).hasSize(TOTAL_DIMENSIONS);
            assertThat(vector[GENRE_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[GENRE_OFFSET + 1]).isEqualTo(1.0f);
            assertThat(vector[GENRE_OFFSET + 2]).isEqualTo(0.0f);

            assertThat(countOnes(vector)).isEqualTo(2);
        }

        @Test
        @DisplayName("테마 → THEME_OFFSET 기준으로 올바른 위치에 1.0")
        void themes_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(), List.of(theme(10L), theme(11L)),
                    List.of(), List.of(), List.of()
            );

            assertThat(vector[THEME_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[THEME_OFFSET + 3]).isEqualTo(1.0f);
            assertThat(vector[THEME_OFFSET + 1]).isEqualTo(0.0f);

            assertThat(countOnes(vector)).isEqualTo(2);

        }

        @Test
        @DisplayName("키워드 → KEYWORD_OFFSET 기준으로 올바른 위치에 1.0")
        void keywords_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(), List.of(),
                    List.of(keyword(100L), keyword(102L)),
                    List.of(), List.of()
            );

            assertThat(vector[KEYWORD_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[KEYWORD_OFFSET + 99]).isEqualTo(1.0f);
            assertThat(vector[KEYWORD_OFFSET + 50]).isEqualTo(0.0f);

            assertThat(countOnes(vector)).isEqualTo(2);

        }

        @Test
        @DisplayName("게임모드 → MODE_OFFSET 기준으로 올바른 위치에 1.0")
        void gameModes_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(), List.of(), List.of(),
                    List.of(gameMode(201L)),
                    List.of()
            );

            assertThat(vector[MODE_OFFSET + 2]).isEqualTo(1.0f);
            assertThat(vector[MODE_OFFSET + 0]).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("플레이어 시점 → PERSPECTIVE_OFFSET 기준으로 올바른 위치에 1.0")
        void perspectives_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(), List.of(), List.of(), List.of(),
                    List.of(perspective(301L))
            );

            assertThat(vector[PERSPECTIVE_OFFSET + 4]).isEqualTo(1.0f);
            assertThat(vector[PERSPECTIVE_OFFSET + 0]).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("5종 속성을 모두 넣으면 각 구간에 올바르게 1.0이 설정된다")
        void allAttributes_setCorrectPositions() {
            float[] vector = service.buildFeatureVector(
                    List.of(genre(1L)),
                    List.of(theme(10L)),
                    List.of(keyword(101L)),
                    List.of(gameMode(200L)),
                    List.of(perspective(300L))
            );

            assertThat(vector).hasSize(TOTAL_DIMENSIONS);
            assertThat(vector[GENRE_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[THEME_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[KEYWORD_OFFSET + 50]).isEqualTo(1.0f);
            assertThat(vector[MODE_OFFSET + 0]).isEqualTo(1.0f);
            assertThat(vector[PERSPECTIVE_OFFSET + 0]).isEqualTo(1.0f);

            long oneCount = 0;
            for (float v : vector) {
                if (v == 1.0f) oneCount++;
            }
            assertThat(oneCount).isEqualTo(5);
        }

        @Test
        @DisplayName("매핑에 없는 id는 무시하고 벡터에 반영하지 않는다")
        void unknownId_isIgnored() {
            float[] vector = service.buildFeatureVector(
                    List.of(genre(999L)),
                    List.of(), List.of(), List.of(), List.of()
            );

            assertThat(vector).hasSize(TOTAL_DIMENSIONS);
            assertThat(vector).containsOnly(0.0f);
        }

        @Test
        @DisplayName("매핑에 있는 id와 없는 id가 섞여있으면 있는 것만 반영된다")
        void mixedKnownAndUnknown_onlyKnownApplied() {
            float[] vector = service.buildFeatureVector(
                    List.of(genre(1L), genre(999L)),
                    List.of(), List.of(), List.of(), List.of()
            );

            assertThat(vector[GENRE_OFFSET + 0]).isEqualTo(1.0f);

            long oneCount = 0;
            for (float v : vector) {
                if (v == 1.0f) oneCount++;
            }
            assertThat(oneCount).isEqualTo(1);
        }

        @Test
        @DisplayName("매퍼가 refresh 되지 않은 빈 상태면 전부 0.0")
        void emptyMapper_returnsZeroVector() {
            VectorDimensionMapper emptyMapper = new VectorDimensionMapper();
            GameVectorService emptyService = new GameVectorService(emptyMapper);

            float[] vector = emptyService.buildFeatureVector(
                    List.of(genre(1L)),
                    List.of(theme(10L)),
                    List.of(keyword(100L)),
                    List.of(gameMode(200L)),
                    List.of(perspective(300L))
            );

            assertThat(vector).hasSize(TOTAL_DIMENSIONS);
            assertThat(vector).containsOnly(0.0f);
        }

        @Test
        @DisplayName("같은 속성 구간 내에서 인덱스가 겹치지 않는다")
        void noOverlapBetweenSections() {
            float[] vector = service.buildFeatureVector(
                    List.of(genre(3L)),          // index 5
                    List.of(theme(11L)),         // index 3
                    List.of(keyword(102L)),      // index 99
                    List.of(gameMode(201L)),     // index 2
                    List.of(perspective(301L))   // index 4
            );

            // 각 속성이 자기 구간에만 영향을 주는지 확인
            assertThat(vector[GENRE_OFFSET + 5]).isEqualTo(1.0f);
            assertThat(vector[THEME_OFFSET + 3]).isEqualTo(1.0f);
            assertThat(vector[KEYWORD_OFFSET + 99]).isEqualTo(1.0f);
            assertThat(vector[MODE_OFFSET + 2]).isEqualTo(1.0f);
            assertThat(vector[PERSPECTIVE_OFFSET + 4]).isEqualTo(1.0f);

            // Genre 구간의 index 3 위치는 0.0이어야 함 (Theme의 index 3과 혼동 없음)
            assertThat(vector[GENRE_OFFSET + 3]).isEqualTo(0.0f);
        }
    }

    @Nested
    @DisplayName("vectorToString")
    class VectorToString {

        @Test
        @DisplayName("pgvector 포맷 [v0,v1,...] 형식으로 변환된다")
        void convertsToCorrectFormat() {
            float[] vector = new float[]{1.0f, 0.0f, 0.5f};

            String result = GameVectorService.vectorToString(vector);

            assertThat(result).isEqualTo("[1.0,0.0,0.5]");
        }

        @Test
        @DisplayName("빈 배열은 []로 변환된다")
        void emptyArray_returnsEmptyBrackets() {
            String result = GameVectorService.vectorToString(new float[]{});

            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("TOTAL_DIMENSIONS 크기 벡터도 올바르게 변환된다")
        void fullDimensionVector_converts() {
            float[] vector = new float[TOTAL_DIMENSIONS];
            vector[0] = 1.0f;
            vector[TOTAL_DIMENSIONS - 1] = 1.0f;

            String result = GameVectorService.vectorToString(vector);

            assertThat(result).startsWith("[1.0,");
            assertThat(result).endsWith(",1.0]");
            long commaCount = result.chars().filter(c -> c == ',').count();
            assertThat(commaCount).isEqualTo(TOTAL_DIMENSIONS - 1);
        }
    }

    // --- vector에서 1.0f count 헬퍼 ---
    private static long countOnes(float[] vector) {
        long c = 0;
        for (float v : vector) if (v == 1.0f) c++;
        return c;
    }

    // --- 테스트용 엔티티 팩토리 ---

    private static Genre genre(Long id) {
        return Genre.builder().id(id).igdbId(id * 10).name("Genre" + id).build();
    }

    private static Theme theme(Long id) {
        return Theme.builder().id(id).igdbId(id * 10).name("Theme" + id).build();
    }

    private static Keyword keyword(Long id) {
        return Keyword.builder().id(id).igdbId(id * 10).name("Keyword" + id).build();
    }

    private static GameMode gameMode(Long id) {
        return GameMode.builder().id(id).igdbId(id * 10).name("GameMode" + id).build();
    }

    private static PlayerPerspective perspective(Long id) {
        return PlayerPerspective.builder().id(id).igdbId(id * 10).name("Perspective" + id).build();
    }
}
