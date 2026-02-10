package com.back.global.batch.processor;

import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.batch.dto.GameBatchItem;
import com.back.global.igdb.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class IgdbGameProcessor implements ItemProcessor<IgdbGameDetailDto, GameBatchItem> {

    private final Map<Long, Genre> genreCache = new ConcurrentHashMap<>();
    private final Map<Long, Platform> platformCache = new ConcurrentHashMap<>();
    private final Map<Long, Theme> themeCache = new ConcurrentHashMap<>();
    private final Map<Long, GameMode> gameModeCache = new ConcurrentHashMap<>();
    private final Map<Long, PlayerPerspective> playerPerspectiveCache = new ConcurrentHashMap<>();
    private final Map<Long, Keyword> keywordCache = new ConcurrentHashMap<>();
    private final Map<Long, Company> companyCache = new ConcurrentHashMap<>();

    public IgdbGameProcessor(GenreRepository genreRepository,
                             PlatformRepository platformRepository,
                             ThemeRepository themeRepository,
                             GameModeRepository gameModeRepository,
                             PlayerPerspectiveRepository playerPerspectiveRepository,
                             KeywordRepository keywordRepository,
                             CompanyRepository companyRepository) {
        genreRepository.findAll().forEach(g -> genreCache.put(g.getIgdbId(), g));
        platformRepository.findAll().forEach(p -> platformCache.put(p.getIgdbId(), p));
        themeRepository.findAll().forEach(t -> themeCache.put(t.getIgdbId(), t));
        gameModeRepository.findAll().forEach(gm -> gameModeCache.put(gm.getIgdbId(), gm));
        playerPerspectiveRepository.findAll().forEach(pp -> playerPerspectiveCache.put(pp.getIgdbId(), pp));
        keywordRepository.findAll().forEach(k -> keywordCache.put(k.getIgdbId(), k));
        companyRepository.findAll().forEach(c -> companyCache.put(c.getIgdbId(), c));

        log.info("프로세서 캐시 초기화: 장르 {}건, 플랫폼 {}건, 테마 {}건, 게임모드 {}건, 시점 {}건, 키워드 {}건, 회사 {}건",
                genreCache.size(), platformCache.size(), themeCache.size(),
                gameModeCache.size(), playerPerspectiveCache.size(),
                keywordCache.size(), companyCache.size());
    }

    @Override
    public GameBatchItem process(IgdbGameDetailDto dto) {
        if (dto.name() == null) {
            return null;
        }

        String summary = dto.summary() != null ? dto.summary() : "";
        String storyline = dto.storyline();

        String coverImageId = dto.cover() != null ? dto.cover().imageId() : null;

        // Extract franchise info (first one only)
        Long franchiseIgdbId = null;
        String franchiseName = null;
        if (dto.franchises() != null && !dto.franchises().isEmpty()) {
            IgdbFranchiseDto first = dto.franchises().get(0);
            franchiseIgdbId = first.id();
            franchiseName = first.name();
        }

        Game game = Game.createGame(
                dto.id(),
                dto.name(),
                summary,
                coverImageId,
                dto.firstReleaseDateEpochSeconds(),
                storyline,
                dto.aggregatedRating(),
                franchiseIgdbId,
                franchiseName
        );

        List<Genre> genres = new ArrayList<>();
        List<Platform> platforms = new ArrayList<>();
        List<Theme> themes = new ArrayList<>();
        List<Keyword> keywords = new ArrayList<>();
        List<GameMode> gameModes = new ArrayList<>();
        List<PlayerPerspective> playerPerspectives = new ArrayList<>();
        List<GameBatchItem.CompanyRoleEntry> companies = new ArrayList<>();
        List<GameBatchItem.ExternalIdEntry> externalIds = new ArrayList<>();

        // Genres
        if (dto.genres() != null) {
            for (IgdbGenreDto g : dto.genres()) {
                Genre genre = genreCache.get(g.id());
                if (genre != null) {
                    genres.add(genre);
                }
            }
        }

        // Platforms
        if (dto.platforms() != null) {
            for (IgdbPlatformDto p : dto.platforms()) {
                Platform platform = platformCache.get(p.id());
                if (platform != null) {
                    platforms.add(platform);
                }
            }
        }

        // Themes
        if (dto.themes() != null) {
            for (IgdbThemeDto t : dto.themes()) {
                Theme theme = themeCache.get(t.id());
                if (theme != null) {
                    themes.add(theme);
                }
            }
        }

        // Keywords
        if (dto.keywords() != null) {
            for (IgdbKeywordDto k : dto.keywords()) {
                Keyword keyword = keywordCache.get(k.id());
                if (keyword != null) {
                    keywords.add(keyword);
                }
            }
        }

        // Game Modes
        if (dto.gameModes() != null) {
            for (IgdbGameModeDto gm : dto.gameModes()) {
                GameMode gameMode = gameModeCache.get(gm.id());
                if (gameMode != null) {
                    gameModes.add(gameMode);
                }
            }
        }

        // Player Perspectives
        if (dto.playerPerspectives() != null) {
            for (IgdbPlayerPerspectiveDto pp : dto.playerPerspectives()) {
                PlayerPerspective playerPerspective = playerPerspectiveCache.get(pp.id());
                if (playerPerspective != null) {
                    playerPerspectives.add(playerPerspective);
                }
            }
        }

        // Involved Companies (deduplicate by company + role)
        if (dto.involvedCompanies() != null) {
            Set<Long> developers = new HashSet<>();
            Set<Long> publishers = new HashSet<>();
            for (IgdbInvolvedCompanyDto ic : dto.involvedCompanies()) {
                if (ic.company() == null) continue;
                Company company = companyCache.get(ic.company().id());
                if (company == null) continue;
                if (Boolean.TRUE.equals(ic.developer()) && developers.add(company.getIgdbId())) {
                    companies.add(new GameBatchItem.CompanyRoleEntry(company, CompanyRole.DEVELOPER));
                }
                if (Boolean.TRUE.equals(ic.publisher()) && publishers.add(company.getIgdbId())) {
                    companies.add(new GameBatchItem.CompanyRoleEntry(company, CompanyRole.PUBLISHER));
                }
            }
        }

        // External Games (Steam only: category == 1)
        if (dto.externalGames() != null) {
            for (IgdbExternalGameDto eg : dto.externalGames()) {
                if (Integer.valueOf(1).equals(eg.category()) && eg.uid() != null) {
                    externalIds.add(new GameBatchItem.ExternalIdEntry("STEAM", eg.uid()));
                }
            }
        }

        return GameBatchItem.builder()
                .game(game)
                .genres(genres)
                .platforms(platforms)
                .themes(themes)
                .keywords(keywords)
                .gameModes(gameModes)
                .playerPerspectives(playerPerspectives)
                .companies(companies)
                .externalIds(externalIds)
                .build();
    }
}
