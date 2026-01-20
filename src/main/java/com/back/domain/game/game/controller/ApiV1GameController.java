package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchResponse;
import com.back.domain.game.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class ApiV1GameController {
    private final GameService gameService;

    @GetMapping
    public List<GameSearchResponse> search(@RequestParam String query) {
        List<GameSearchResponse> searched = gameService.search(query);
        return searched;
    }

    @GetMapping("/{igdbId}")
    public GameDetailResponse getGame(@PathVariable Long igdbId) {
        GameDetailResponse game = gameService.getGameDetail(igdbId);

        return game;
    }
}