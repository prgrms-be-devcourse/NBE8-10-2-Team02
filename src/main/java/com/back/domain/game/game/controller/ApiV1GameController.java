package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameSearchCondition;
import com.back.domain.game.game.dto.GameSearchResponse;
import com.back.domain.game.game.dto.GenreResponse;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.domain.game.game.service.GameSearchService;
import com.back.domain.game.game.service.GameService;

import com.back.domain.game.platform.PlatformGroup;
import com.back.domain.game.game.dto.PlatformResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1GameController {
    private final GameService gameService;
    private final GameSearchService gameSearchService;
    private final GenreRepository genreRepository;

    @GetMapping("/games/{igdbId}")
    public GameDetailResponse getGame(@PathVariable Long igdbId) {
        GameDetailResponse game = gameService.getGameDetail(igdbId);

        return game;
    }

    //    슬기구현
    @GetMapping("/games/search")
    public List<GameSearchResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) List<Long> genre,
            @RequestParam(required = false) String  platform
    ) {
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery(query);
        condition.setGenreIds(genre);
        condition.setPlatformCode(platform);

        return gameSearchService.search(condition);
    }

//    장르 필터링
    @GetMapping("/genres")
    public List<GenreResponse> getGenres() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreResponse(g.getIgdbId(), g.getName()))
                .toList();
    }

//    플랫폼 필터링
    @GetMapping("/platforms")
    public List<PlatformResponse> getPlatforms() {
        return PlatformGroup.DISPLAY_NAME.entrySet().stream()
                .map(e -> new PlatformResponse(
                        e.getKey(),
                        e.getValue()
                ))
                .toList();
    }
}