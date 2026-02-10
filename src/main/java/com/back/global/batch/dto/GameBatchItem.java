package com.back.global.batch.dto;

import com.back.domain.game.game.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameBatchItem {
    private final Game game;
    private final List<Genre> genres;
    private final List<Platform> platforms;
    private final List<Theme> themes;
    private final List<Keyword> keywords;
    private final List<GameMode> gameModes;
    private final List<PlayerPerspective> playerPerspectives;
    private final List<CompanyRoleEntry> companies;
    private final List<ExternalIdEntry> externalIds;

    public record CompanyRoleEntry(Company company, CompanyRole role) {}
    public record ExternalIdEntry(String platform, String externalId) {}
}
