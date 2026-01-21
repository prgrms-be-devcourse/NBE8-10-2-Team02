package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchByNameResponse;
import com.back.domain.game.game.service.GameService;
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
    public GameDetailResponse getGame(@PathVariable Long igdbId) {
        GameDetailResponse game = gameService.getGameDetail(igdbId);

        return game;
    }
}