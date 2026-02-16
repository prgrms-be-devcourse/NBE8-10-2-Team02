package com.back.domain.game.recommendation.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.domain.game.gameLike.entity.GameLike;
import com.back.domain.game.gameLike.repository.GameLikeRepository;
import com.back.domain.game.recommendation.dto.GameRecommendationResponse;
import com.back.domain.game.recommendation.repository.GameVectorRepository;
import com.back.domain.game.recommendation.repository.MemberVectorRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.domain.member.memberGame.repository.MemberGameRepository;
import com.back.global.vector.VectorDimensionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.back.global.vector.VectorDimensionMapper.TOTAL_DIMENSIONS;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class GameRecommendationIntegrationTest {

    @PersistenceContext
    EntityManager em;

    @Autowired GameRepository gameRepository;
    @Autowired GameVectorRepository gameVectorRepository;
    @Autowired MemberVectorRepository memberVectorRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberGameRepository memberGameRepository;
    @Autowired GameLikeRepository gameLikeRepository;
    @Autowired GameRecommendationService recommendationService;
    @Autowired UserProfileVectorService userProfileVectorService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("test@test.com", "password", "tester"));
    }

    @Nested
    @DisplayName("코사인 유사도 기반 유사 게임 조회")
    class SimilarGames {

        @Test
        @DisplayName("같은 장르 벡터를 가진 게임이 다른 장르보다 유사도가 높다")
        void sameGenre_higherSimilarity() {
            // given: RPG 게임(target), RPG 게임(같은 장르), FPS 게임(다른 장르)
            Game rpgTarget = saveGameWithVector(1L, "RPG Target", rpgVector());
            Game rpgSimilar = saveGameWithVector(2L, "RPG Similar", rpgVector());
            Game fpsGame = saveGameWithVector(3L, "FPS Game", fpsVector());

            // when
            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(1L, 10);

            // then: RPG Similar가 FPS Game보다 유사도 높음
            assertThat(results).hasSizeGreaterThanOrEqualTo(2);
            GameRecommendationResponse rpgResult = findByName(results, "RPG Similar");
            GameRecommendationResponse fpsResult = findByName(results, "FPS Game");
            assertThat(rpgResult.similarity()).isGreaterThan(fpsResult.similarity());
        }

        @Test
        @DisplayName("자기 자신은 유사 게임 결과에 포함되지 않는다")
        void excludesSelf() {
            Game target = saveGameWithVector(10L, "Target", rpgVector());
            Game other = saveGameWithVector(11L, "Other", rpgVector());

            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(10L, 10);

            assertThat(results).extracting(GameRecommendationResponse::name)
                    .doesNotContain("Target")
                    .contains("Other");
        }

        @Test
        @DisplayName("feature_vector가 NULL인 게임은 결과에 포함되지 않는다")
        void nullVector_excluded() {
            Game withVector = saveGameWithVector(20L, "WithVector", rpgVector());
            Game target = saveGameWithVector(21L, "Target", rpgVector());
            // NULL 벡터 게임 (벡터 업데이트 안 함)
            Game noVector = gameRepository.save(
                    Game.createGame(22L, "NoVector", "summary", null, 1700000000L, null, null, null, null));

            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(21L, 10);

            assertThat(results).extracting(GameRecommendationResponse::name)
                    .contains("WithVector")
                    .doesNotContain("NoVector");
        }

        @Test
        @DisplayName("동일 벡터의 코사인 유사도는 1.0이다")
        void identicalVectors_similarityIsOne() {
            Game target = saveGameWithVector(30L, "Target", rpgVector());
            Game identical = saveGameWithVector(31L, "Identical", rpgVector());

            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(30L, 10);

            GameRecommendationResponse result = findByName(results, "Identical");
            assertThat(result.similarity()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.01));
        }

        @Test
        @DisplayName("직교 벡터(겹치는 장르 없음)의 코사인 유사도는 0.0이다")
        void orthogonalVectors_similarityIsZero() {
            Game target = saveGameWithVector(40L, "Target", rpgVector());
            Game orthogonal = saveGameWithVector(41L, "Orthogonal", fpsVector());

            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(40L, 10);

            GameRecommendationResponse result = findByName(results, "Orthogonal");
            assertThat(result.similarity()).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.01));
        }
    }

    @Nested
    @DisplayName("개인 맞춤 추천")
    class PersonalRecommendations {

        @Test
        @DisplayName("RPG 프로필 유저에게 RPG 게임이 FPS 게임보다 상위 추천된다")
        void rpgProfile_prefersRpgGames() {
            // given: 유저가 RPG 게임을 보유 → 프로필 벡터 = RPG
            Game ownedRpg = saveGameWithVector(50L, "Owned RPG", rpgVector());
            memberGameRepository.save(
                    new MemberGame(1L, 100, true, StatusEnum.COMPLETED, member, ownedRpg));

            // 추천 후보: RPG 3개 + FPS 3개
            Game rpg1 = saveGameWithVector(51L, "RPG Candidate 1", rpgVector());
            Game rpg2 = saveGameWithVector(52L, "RPG Candidate 2", rpgVector());
            Game rpg3 = saveGameWithVector(53L, "RPG Candidate 3", rpgVector());
            Game fps1 = saveGameWithVector(54L, "FPS Candidate 1", fpsVector());
            Game fps2 = saveGameWithVector(55L, "FPS Candidate 2", fpsVector());
            Game fps3 = saveGameWithVector(56L, "FPS Candidate 3", fpsVector());

            // 프로필 벡터 생성
            userProfileVectorService.calculateAndSaveProfileVector(member.getId());

            // when
            List<GameRecommendationResponse> results =
                    recommendationService.getPersonalRecommendations(member.getId(), 6);

            // then: 상위 3개가 RPG
            assertThat(results).hasSize(6);
            List<String> topThreeNames = results.subList(0, 3).stream()
                    .map(GameRecommendationResponse::name).toList();
            assertThat(topThreeNames).allMatch(name -> name.startsWith("RPG"));
        }

        @Test
        @DisplayName("보유 게임은 추천 결과에서 제외된다")
        void ownedGames_excluded() {
            Game owned = saveGameWithVector(60L, "Owned", rpgVector());
            Game candidate = saveGameWithVector(61L, "Candidate", rpgVector());
            memberGameRepository.save(
                    new MemberGame(1L, 0, false, StatusEnum.PLAYING, member, owned));

            userProfileVectorService.calculateAndSaveProfileVector(member.getId());

            List<GameRecommendationResponse> results =
                    recommendationService.getPersonalRecommendations(member.getId(), 10);

            assertThat(results).extracting(GameRecommendationResponse::name)
                    .doesNotContain("Owned")
                    .contains("Candidate");
        }

        @Test
        @DisplayName("하이브리드 스코어가 높은 게임이 상위에 온다 (similarity + rating + likeCount)")
        void hybridScore_affectsRanking() {
            // 유저 프로필: RPG
            Game owned = saveGameWithVector(70L, "Owned", rpgVector());
            memberGameRepository.save(
                    new MemberGame(1L, 0, false, StatusEnum.PLAYING, member, owned));

            // 두 RPG 게임: 유사도 동일하지만 rating/likeCount 다름
            Game highRated = saveGameWithVector(71L, "HighRated RPG", rpgVector(), 95.0, 1000);
            Game lowRated = saveGameWithVector(72L, "LowRated RPG", rpgVector(), 10.0, 0);

            userProfileVectorService.calculateAndSaveProfileVector(member.getId());

            List<GameRecommendationResponse> results =
                    recommendationService.getPersonalRecommendations(member.getId(), 2);

            assertThat(results.get(0).name()).isEqualTo("HighRated RPG");
            assertThat(results.get(0).score()).isGreaterThan(results.get(1).score());
        }
    }

    @Nested
    @DisplayName("프로필 벡터 + 가중치 통합 검증")
    class ProfileVectorIntegration {

        @Test
        @DisplayName("즐겨찾기 + COMPLETED 게임이 프로필 벡터에 더 강하게 반영된다")
        void favoriteCompleted_strongerInfluence() {
            // RPG 게임: 즐겨찾기 + COMPLETED (높은 가중치)
            // FPS 게임: PLAN_TO_PLAY (낮은 가중치)
            Game rpg = saveGameWithVector(80L, "Fav RPG", rpgVector());
            Game fps = saveGameWithVector(81L, "Plan FPS", fpsVector());
            memberGameRepository.save(
                    new MemberGame(1L, 200, true, StatusEnum.COMPLETED, member, rpg));
            memberGameRepository.save(
                    new MemberGame(1L, 0, false, StatusEnum.PLAN_TO_PLAY, member, fps));

            // 추천 후보
            Game rpgCandidate = saveGameWithVector(82L, "RPG Candidate", rpgVector());
            Game fpsCandidate = saveGameWithVector(83L, "FPS Candidate", fpsVector());

            userProfileVectorService.calculateAndSaveProfileVector(member.getId());

            List<GameRecommendationResponse> results =
                    recommendationService.getPersonalRecommendations(member.getId(), 2);

            // RPG 가중치가 훨씬 높으므로 RPG가 1위
            assertThat(results.get(0).name()).isEqualTo("RPG Candidate");
        }

        @Test
        @DisplayName("좋아요한 게임이 프로필 벡터에 1.3배 반영된다")
        void likedGame_boosted() {
            // 두 게임 모두 PLAYING, 동일 조건이지만 하나만 좋아요
            Game rpg = saveGameWithVector(90L, "Liked RPG", rpgVector());
            Game fps = saveGameWithVector(91L, "Unliked FPS", fpsVector());
            memberGameRepository.save(
                    new MemberGame(1L, 0, false, StatusEnum.PLAYING, member, rpg));
            memberGameRepository.save(
                    new MemberGame(1L, 0, false, StatusEnum.PLAYING, member, fps));
            // RPG에만 좋아요
            gameLikeRepository.save(GameLike.createGameLike(member, rpg));

            Game rpgCandidate = saveGameWithVector(92L, "RPG Candidate", rpgVector());
            Game fpsCandidate = saveGameWithVector(93L, "FPS Candidate", fpsVector());

            userProfileVectorService.calculateAndSaveProfileVector(member.getId());

            List<GameRecommendationResponse> results =
                    recommendationService.getPersonalRecommendations(member.getId(), 2);

            // 좋아요 가중치(1.3×)로 RPG 방향이 더 강함
            assertThat(results.get(0).name()).isEqualTo("RPG Candidate");
        }
    }

    @Nested
    @DisplayName("벌크 벡터 업데이트")
    class BulkVectorUpdate {

        @Test
        @DisplayName("bulkUpdateFeatureVectors로 저장한 벡터가 유사도 검색에 사용된다")
        void bulkUpdate_thenSearchWorks() {
            Game game1 = gameRepository.save(
                    Game.createGame(100L, "Bulk1", "s", null, 1700000000L, null, null, null, null));
            Game game2 = gameRepository.save(
                    Game.createGame(101L, "Bulk2", "s", null, 1700000000L, null, null, null, null));

            em.flush();
            // bulkUpdate로 벡터 저장 (내부에서 flush 후 JDBC batch UPDATE)
            gameVectorRepository.bulkUpdateFeatureVectors(Map.of(
                    game1.getId(), vectorToString(rpgVector()),
                    game2.getId(), vectorToString(fpsVector())
            ));
            em.clear();
            // rpgVector 기준 검색 → Bulk1(RPG)이 Bulk2(FPS)보다 유사도 높음
            Game searchTarget = saveGameWithVector(102L, "SearchTarget", rpgVector());
            List<GameRecommendationResponse> results = recommendationService.getSimilarGames(102L, 10);

            GameRecommendationResponse bulk1 = findByName(results, "Bulk1");
            GameRecommendationResponse bulk2 = findByName(results, "Bulk2");
            assertThat(bulk1.similarity()).isGreaterThan(bulk2.similarity());
        }
    }

    // --- 벡터 헬퍼 ---

    /**
     * RPG 벡터: Genre index 0 = 1.0 (나머지 0)
     */
    private float[] rpgVector() {
        float[] v = new float[TOTAL_DIMENSIONS];
        v[VectorDimensionMapper.GENRE_OFFSET] = 1.0f;
        return v;
    }

    /**
     * FPS 벡터: Genre index 1 = 1.0 (나머지 0) — RPG와 직교
     */
    private float[] fpsVector() {
        float[] v = new float[TOTAL_DIMENSIONS];
        v[VectorDimensionMapper.GENRE_OFFSET + 1] = 1.0f;
        return v;
    }

    private Game saveGameWithVector(long igdbId, String name, float[] vector) {
        return saveGameWithVector(igdbId, name, vector, 0.0, 0);
    }

    private Game saveGameWithVector(long igdbId, String name, float[] vector,
                                     Double rating, long likeCount) {
        Game game = Game.createGame(igdbId, name, "summary", null, 1700000000L,
                null, rating, null, null);
        game = gameRepository.save(game);

        // bulkUpdateFeatureVectors 내부에서 flush 후 JDBC batch UPDATE 실행
        gameVectorRepository.bulkUpdateFeatureVectors(
                Map.of(game.getId(), vectorToString(vector)));
        em.clear();
        return game;
    }

    private static String vectorToString(float[] vector) {
        return GameVectorService.vectorToString(vector);
    }

    private static GameRecommendationResponse findByName(
            List<GameRecommendationResponse> results, String name) {
        return results.stream()
                .filter(r -> r.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("결과에 '" + name + "' 게임이 없습니다. "
                        + "실제 결과: " + results.stream().map(GameRecommendationResponse::name).toList()));
    }
}
