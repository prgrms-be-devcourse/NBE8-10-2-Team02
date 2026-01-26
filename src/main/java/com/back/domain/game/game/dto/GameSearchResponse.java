package com.back.domain.game.game.dto;

import com.back.global.igdb.dto.IgdbGameSummaryDto;
import com.back.domain.game.game.entity.Game;
import com.back.global.igdb.dto.IgdbPlatformDto;
import com.back.standard.util.TimeUt;

import com.back.global.igdb.util.IgdbImageUtil;

import java.time.LocalDate;
import java.util.List;

import java.util.Map;
import java.util.Objects;

import static com.back.domain.game.platform.PlatformGroup.PLATFORM_MAP;
import static com.back.domain.game.platform.PlatformGroup.DISPLAY_NAME;


public record GameSearchResponse(
        long igdbId,
        String name,
        String imageUrl,
        LocalDate firstReleaseDate,
        List<String> genres,
        List<String> platforms
        // developerName 추가예정
) {
    public static GameSearchResponse fromDto(
            IgdbGameSummaryDto d,
            Map<Long, String> genreMap,
            Map<Long, String> platformMap
    ) {
        return new GameSearchResponse(
                d.id(),
                d.name(),
                IgdbImageUtil.cover(
                        d.cover() != null ? d.cover().imageId() : null
                ),
                TimeUt.epoch.toLocalDate(d.firstReleaseDateEpochSeconds()),
                // 장르 null 체크
                d.genres() == null ? List.of() : d.genres().stream()
                        .map(genreMap::get)
                        .filter(Objects::nonNull)
                        .toList(),
                // 플랫폼 null 체크
                d.platforms() == null ? List.of() : d.platforms().stream()
                        .map(platformMap::get)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

}
