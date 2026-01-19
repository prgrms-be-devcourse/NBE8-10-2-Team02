package com.back.domain.game.game.dto;

import com.back.global.igdb.dto.IgdbGameDto;
import com.back.standard.util.TimeUt;

import java.time.LocalDate;

public record GameSearchRes(
//        int gameId,
        long igdbId,
        String name,
        String summary,
        LocalDate firstReleaseDate
) {
    public static GameSearchRes fromDto(IgdbGameDto d) {
        return new GameSearchRes(
                d.id(),
                d.name(),
                d.summary(),
                TimeUt.epoch.toLocalDate(d.firstReleaseDateEpochSeconds())
        );
    }
}
