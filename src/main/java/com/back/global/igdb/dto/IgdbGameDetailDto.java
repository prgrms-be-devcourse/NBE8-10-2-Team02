package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IgdbGameDetailDto(
        long id,
        String name,
        String summary,
        @JsonProperty("first_release_date")
        Long firstReleaseDateEpochSeconds,
        IgdbCoverDto cover,
        @JsonProperty("involved_companies")
        List<IgdbInvolvedCompanyDto> involvedCompanies,
        List<IgdbGenreDto> genres,
        List<IgdbPlatformDto> platforms,
        String storyline,
        List<IgdbThemeDto> themes,
        List<IgdbKeywordDto> keywords,
        @JsonProperty("game_modes")
        List<IgdbGameModeDto> gameModes,
        @JsonProperty("player_perspectives")
        List<IgdbPlayerPerspectiveDto> playerPerspectives,
        @JsonProperty("external_games")
        List<IgdbExternalGameDto> externalGames,
        List<IgdbFranchiseDto> franchises,
        @JsonProperty("aggregated_rating")
        Double aggregatedRating
) {

}
