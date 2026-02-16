package com.back.domain.game.recommendation.service;

import com.back.domain.game.gameLike.repository.GameLikeRepository;
import com.back.domain.game.recommendation.repository.GameVectorRepository;
import com.back.domain.game.recommendation.repository.MemberVectorRepository;
import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.domain.member.memberGame.repository.MemberGameRepository;
import com.back.domain.review.entity.Review;
import com.back.global.vector.VectorDimensionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileVectorService {

    private final MemberGameRepository memberGameRepository;
    private final GameLikeRepository gameLikeRepository;
    private final GameVectorRepository gameVectorRepository;
    private final MemberVectorRepository memberVectorRepository;

    @Transactional
    public void calculateAndSaveProfileVector(int memberId) {
        List<MemberGame> memberGames = memberGameRepository.findAllByMemberId(memberId);
        if (memberGames.isEmpty()) {
            memberVectorRepository.updateProfileVector(memberId, null);
            return;
        }

        // DB에 저장된 feature_vector를 한 번의 쿼리로 조회 (기존: 게임당 5쿼리 → 1쿼리)
        Map<Integer, float[]> gameVectorMap = loadFeatureVectors(memberId);

        Set<Integer> likedGameIds = gameLikeRepository.findGameIdsByMemberId(memberId)
                .stream().collect(Collectors.toSet());

        float[] profileVector = new float[VectorDimensionMapper.TOTAL_DIMENSIONS];
        double totalWeight = 0.0;

        for (MemberGame mg : memberGames) {
            float[] gameVector = gameVectorMap.get(mg.getGame().getId());
            if (gameVector == null) {
                continue;
            }

            double weight = calculateWeight(mg, likedGameIds.contains(mg.getGame().getId()));

            for (int i = 0; i < VectorDimensionMapper.TOTAL_DIMENSIONS; i++) {
                profileVector[i] += (float) (gameVector[i] * weight);
            }
            totalWeight += weight;
        }

        // 가중 평균
        if (totalWeight > 0) {
            for (int i = 0; i < VectorDimensionMapper.TOTAL_DIMENSIONS; i++) {
                profileVector[i] /= (float) totalWeight;
            }
        }

        String vectorString = GameVectorService.vectorToString(profileVector);
        memberVectorRepository.updateProfileVector(memberId, vectorString);
        log.debug("프로필 벡터 갱신 완료: memberId={}", memberId);
    }

    private Map<Integer, float[]> loadFeatureVectors(int memberId) {
        List<Object[]> rows = gameVectorRepository.findFeatureVectorsByMemberId(memberId);
        Map<Integer, float[]> map = new HashMap<>(rows.size());
        for (Object[] row : rows) {
            int gameId = (int) row[0];
            String vectorStr = (String) row[1];
            map.put(gameId, parseVector(vectorStr));
        }
        return map;
    }

    private static float[] parseVector(String vectorStr) {
        // pgvector format: "[0.0,1.0,0.0,...]"
        String inner = vectorStr.substring(1, vectorStr.length() - 1);
        String[] parts = inner.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }

    private double calculateWeight(MemberGame mg, boolean isLiked) {
        double weight = 1.0;

        // 즐겨찾기 가중치
        if (mg.isFavorite()) {
            weight *= 2.5;
        }

        // 상태 기반 가중치
        weight *= getStatusWeight(mg.getStatus());

        // 플레이타임 가중치 (log scale)
        if (mg.getPlaytime() > 0) {
            weight *= (1.0 + Math.log1p(mg.getPlaytime()) * 0.3);
        }

        // 좋아요 가중치
        if (isLiked) {
            weight *= 1.3;
        }

        // 리뷰 평점 가중치
        Review review = mg.getReview();
        if (review != null && review.getRating() > 0) {
            weight *= (review.getRating() / 5.0);
        }

        return weight;
    }

    private double getStatusWeight(StatusEnum status) {
        if (status == null) return 1.0;
        return switch (status) {
            case COMPLETED -> 2.0;
            case PLAYING -> 1.8;
            case ON_HOLD -> 1.2;
            case PLAN_TO_PLAY -> 0.8;
            case DROPPED -> 0.5;
        };
    }
}
