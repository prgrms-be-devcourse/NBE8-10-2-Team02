package com.back.domain.game.game.dto;

import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.standard.util.TimeUt;

import java.time.LocalDate;

public record GameSearchResponse(
        long igdbId,
        String name,
        String summary,
        LocalDate firstReleaseDate
) {
    public static GameSearchResponse fromDto(IgdbGameSummaryDto d) {
        return new GameSearchResponse(
                d.id(),
                d.name(),
                d.summary(),
                TimeUt.epoch.toLocalDate(d.firstReleaseDateEpochSeconds())
        );
    }
}
