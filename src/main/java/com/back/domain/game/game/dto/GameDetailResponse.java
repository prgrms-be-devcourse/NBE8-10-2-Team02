package com.back.domain.game.game.dto;

import com.back.domain.game.game.entity.Game;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.dto.IgdbKeywordDto;
import com.back.global.igdb.dto.IgdbPlatformDto;

import java.time.LocalDate;
import java.util.List;

public record GameDetailResponse(
        int gameId,
        long igdbId,
        String name,
        String summary,
        LocalDate firstReleaseDateEpochSeconds
//        IgdbCoverDto cover,
//        List<IgdbGenreDto> genres,
//        List<IgdbKeywordDto> keywords,
//        List<IgdbPlatformDto> platforms
) {
    public static GameDetailResponse from(Game g) {
        return new GameDetailResponse(
                g.getId(),
                g.getIgdbId(),
                g.getName(),
                g.getSummary(),
                g.getFirstReleaseDate()
        );
    }
}