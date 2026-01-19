package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameSearchRes;
import com.back.domain.game.game.service.GameService;
import com.back.global.igdb.dto.IgdbGameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class ApiV1GameController {
    private final GameService gameService;

    @GetMapping
    public List<GameSearchRes> search(@RequestParam String query) {
        List<GameSearchRes> searched = gameService.search(query);
        return searched;
    }

    @GetMapping("/{igdbId}")
    public IgdbGameDto getGame(@PathVariable Long igdbId) {
        IgdbGameDto game = gameService.getGame(igdbId);

        return game;
    }
}