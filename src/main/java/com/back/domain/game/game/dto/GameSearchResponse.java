package com.back.domain.game.game.dto;

import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.domain.game.game.entity.Game;
import com.back.standard.util.TimeUt;

import com.back.global.igdb.util.IgdbImageUtil;

import java.time.LocalDate;
import java.util.List;

import java.util.Map;
import java.util.Objects;

public record GameSearchResponse(
        long igdbId,
        String name,
        String imageUrl,
        LocalDate firstReleaseDate,
        List<String> genres
        // developerName 추가예정
) {
    public static GameSearchResponse fromDto(
            IgdbGameSummaryDto d,
            Map<Long, String> genreMap
    ) {
        return new GameSearchResponse(
                d.id(),
                d.name(),
                IgdbImageUtil.cover(
                        d.cover() != null ? d.cover().imageId() : null
                ),
                TimeUt.epoch.toLocalDate(d.firstReleaseDateEpochSeconds()),
                d.genres().stream()
                        .map(genreMap::get)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    // DB 엔티티 변환용
    public static GameSearchResponse fromEntity(Game game) {
        return new GameSearchResponse(
                game.getIgdbId(),
                game.getName(),
                IgdbImageUtil.cover(game.getCoverImageId()),
                game.getFirstReleaseDate(),
                game.getGameGenres().stream()
                        .map(gg -> gg.getGenre().getName())
                        .toList()
        );
    }
}