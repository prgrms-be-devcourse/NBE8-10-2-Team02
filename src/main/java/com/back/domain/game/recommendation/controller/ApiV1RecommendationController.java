package com.back.domain.game.recommendation.controller;

import com.back.domain.game.recommendation.dto.GameRecommendationResponse;
import com.back.domain.game.recommendation.service.GameRecommendationService;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "ApiV1RecommendationController", description = "게임 추천 API")
public class ApiV1RecommendationController {

    private final GameRecommendationService gameRecommendationService;
    private final Rq rq;

    @GetMapping("/recommendations")
    @Operation(summary = "개인 맞춤 게임 추천")
    public RsData<List<GameRecommendationResponse>> getRecommendations(
            @RequestParam(defaultValue = "20") int limit
    ) {
        Member actor = rq.getActor();
        if (actor == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }

        List<GameRecommendationResponse> recommendations =
                gameRecommendationService.getPersonalRecommendations(actor.getId(), limit);

        return new RsData<>("200-1", "추천 게임 조회 성공", recommendations);
    }

    @GetMapping("/games/{igdbId}/similar")
    @Operation(summary = "유사 게임 추천")
    public RsData<List<GameRecommendationResponse>> getSimilarGames(
            @PathVariable long igdbId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<GameRecommendationResponse> similarGames =
                gameRecommendationService.getSimilarGames(igdbId, limit);

        return new RsData<>("200-1", "유사 게임 조회 성공", similarGames);
    }
}
