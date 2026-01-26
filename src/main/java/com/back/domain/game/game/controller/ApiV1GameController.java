package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchByNameResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.domain.game.game.service.GameService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class ApiV1GameController {
    private final GameService gameService;

    @GetMapping("/searchByName")
    public List<GameSearchByNameResponse> searchByName(@RequestParam String query) {
        List<GameSearchByNameResponse> searched = gameService.search(query);
        return searched;
    }

    @GetMapping("/{igdbId}")
    public GameDetailResponse getGameDetail(@PathVariable long igdbId) {
        return gameService.getGameDetail(igdbId);
    }

    @GetMapping("/{igdbId}/video")
    public GameVideoResponse getVideoId(@PathVariable long igdbId) {
        return gameService.getVideoId(igdbId);
    }

    @GetMapping("/{igdbId}/similarGames")
    public List<SimilarGameResponse> getSimilarGames(@PathVariable long igdbId) {
        return gameService.getSimilarGames(igdbId);
    }
}