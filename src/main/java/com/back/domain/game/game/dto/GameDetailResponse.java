package com.back.domain.game.game.dto;

import com.back.domain.game.game.entity.Game;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.dto.IgdbKeywordDto;
import com.back.global.igdb.dto.IgdbPlatformDto;
import com.back.standard.util.TimeUt;

import java.time.LocalDate;
import java.util.List;

public record GameDetailResponse(
        long igdbId,
        String gameName,
        String summary,
        LocalDate firstReleaseDate,
        String coverImageId,
        List<String> genres,
        List<String> platforms
) {}