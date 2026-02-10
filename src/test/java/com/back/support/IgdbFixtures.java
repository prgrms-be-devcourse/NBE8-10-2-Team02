package com.back.support;

import com.back.global.igdb.dto.*;

import java.util.List;

public class IgdbFixtures {

    public static IgdbGameDetailDto gameDetail(long igdbId) {
        return new IgdbGameDetailDto(
                igdbId,
                "Test Game " + igdbId,
                "summary-" + igdbId,
                1700000000L,
                new IgdbCoverDto(10000L, "coverImage"),
                List.of(new IgdbInvolvedCompanyDto(
                        10000L,
                        new IgdbCompanyDto(10000L, "testCompany"),
                        true,
                        true)
                ),
                List.of(
                        new IgdbGenreDto(10000L, "Action"),
                        new IgdbGenreDto(10001L, "RPG")
                ),
                List.of(
                        new IgdbPlatformDto(10001L, "PC (Windows)"),
                        new IgdbPlatformDto(20001L, "PlayStation 5")
                ),
                null, // storyline
                null, // themes
                null, // keywords
                null, // gameModes
                null, // playerPerspectives
                null, // externalGames
                null, // franchises
                null  // aggregatedRating
        );
    }
}
