package com.back.domain.game.game.controller;

import com.back.domain.game.game.dto.*;
import com.back.domain.game.game.service.GameService;
import com.back.global.rsData.RsData;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.domain.game.game.service.GameSearchService;
import com.back.domain.game.game.service.GameService;

import com.back.domain.game.game.service.GenreService;
import com.back.domain.game.platform.PlatformGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "ApiV1GameController", description = "게임 검색과 상세조회 API")
public class ApiV1GameController {
    private final GameService gameService;
    private final GameSearchService gameSearchService;
    private final GenreService genreService;

    @GetMapping("/games/{igdbId}")
    @Operation(summary = "게임 상세 조회", description = "IGDB 게임 상세 조회")
    public GameDetailResponse getGameDetail(@PathVariable long igdbId) {
        return gameService.getGameDetail(igdbId);
    }

    @GetMapping("/games/{igdbId}/video")
    @Operation(summary = "게임 영상 조회", description = "게임 상세 정보에 사용될 videoId조회")
    public GameVideoResponse getVideoId(@PathVariable long igdbId) {
        return gameService.getVideoId(igdbId);
    }

    @GetMapping("/games/{igdbId}/similarGames")
    @Operation(summary = "비슷한 게임 조회", description = "게임 상세 정보에 사용될 비슷한 게임 목록 조회")
    public List<SimilarGameResponse> getSimilarGames(@PathVariable long igdbId) {
        return gameService.getSimilarGames(igdbId);
    }

    @GetMapping("/games/popular")
    @Operation(summary = "인기 게임 조회", description = "IGDB + 자체 서비스 데이터 기반 인기 순위")
    public List<PopularGameResponse> getPopularGames(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "hybrid") String source  // "db", "igdb", "hybrid"
    ) {
        return switch (source) {
            case "db" -> gameService.getPopularGames(limit);
            case "hybrid" -> gameService.getPopularGamesHybrid(limit);
            default -> gameService.getPopularGamesHybrid(limit);
        };
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
        return genreService.getGenres();
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