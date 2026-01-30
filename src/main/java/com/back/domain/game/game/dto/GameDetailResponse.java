package com.back.domain.game.game.dto;

import com.back.domain.game.game.entity.Game;

import java.time.LocalDate;
import java.util.List;

public record GameDetailResponse(
        int gameId,
        long igdbId,
        String gameName,
        String summary,
        LocalDate firstReleaseDate,
        String coverImageId,
        String coverUrlTemplate,
        List<String> developers,
        List<String> publishers,
        List<String> genres,
        List<String> platforms
) {
    private static final String COVER_URL_TEMPLATE =
            "https://images.igdb.com/igdb/image/upload/{size}/{id}.jpg";

    public static GameDetailResponse from(Game game, List<String> genres, List<String> platforms) {
        String coverImageId = game.getCoverImageId();
        return new GameDetailResponse(
                game.getId(),
                game.getIgdbId(),
                game.getName(),
                game.getSummary(),
                game.getFirstReleaseDate(),
                coverImageId,
                coverImageId == null ? null : COVER_URL_TEMPLATE,
                game.getDevelopers(),
                game.getPublishers(),
                genres,
                platforms
        );
    }
}