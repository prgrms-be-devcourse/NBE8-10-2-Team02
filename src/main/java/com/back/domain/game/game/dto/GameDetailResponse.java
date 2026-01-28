package com.back.domain.game.game.dto;

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

    public static GameDetailResponse from(
            int gameId,
            long igdbId,
            String gameName,
            String summary,
            LocalDate firstReleaseDate,
            String coverImageId,
            List<String> developers,
            List<String> publishers,
            List<String> genres,
            List<String> platforms
    ) {
        return new GameDetailResponse(
                gameId,
                igdbId,
                gameName,
                summary,
                firstReleaseDate,
                coverImageId,
                coverImageId == null ? null : COVER_URL_TEMPLATE,
                developers,
                publishers,
                genres,
                platforms
        );
    }
}