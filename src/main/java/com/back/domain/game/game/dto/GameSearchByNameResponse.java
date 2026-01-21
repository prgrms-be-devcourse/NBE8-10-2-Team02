package com.back.domain.game.game.dto;

import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.standard.util.TimeUt;

import java.time.LocalDate;

public record GameSearchByNameResponse(
        long igdbId,
        String name,
        String summary,
        LocalDate firstReleaseDate
) {
    public static GameSearchByNameResponse fromDto(IgdbGameSummaryDto d) {
        return new GameSearchByNameResponse(
                d.id(),
                d.name(),
                d.summary(),
                TimeUt.epoch.toLocalDate(d.firstReleaseDateEpochSeconds())
        );
    }
}
