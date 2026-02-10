package com.back.global.batch.writer;

import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.batch.dto.GameBatchItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IgdbGameUpsertWriter implements ItemWriter<GameBatchItem> {

    private final GameRepository gameRepository;
    private final GameGenreRepository gameGenreRepository;
    private final GamePlatformRepository gamePlatformRepository;
    private final GameThemeRepository gameThemeRepository;
    private final GameKeywordRepository gameKeywordRepository;
    private final GameGameModeRepository gameGameModeRepository;
    private final GamePlayerPerspectiveRepository gamePlayerPerspectiveRepository;
    private final GameCompanyRepository gameCompanyRepository;
    private final GameExternalIdRepository gameExternalIdRepository;

    @Override
    public void write(Chunk<? extends GameBatchItem> chunk) {
        // 1. 기존 게임 조회
        List<Long> igdbIds = chunk.getItems().stream()
                .map(item -> item.getGame().getIgdbId())
                .toList();
        Map<Long, Game> existingMap = gameRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(Game::getIgdbId, Function.identity()));

        // 2. 신규/업데이트 분리 + 게임 persist
        Map<Long, Game> persistedMap = new HashMap<>();
        List<Integer> existingGameIds = new ArrayList<>();
        int created = 0, updated = 0;

        for (GameBatchItem item : chunk) {
            Game incoming = item.getGame();
            Game existing = existingMap.get(incoming.getIgdbId());

            if (existing == null) {
                Game saved = gameRepository.save(incoming);
                persistedMap.put(incoming.getIgdbId(), saved);
                created++;
            } else {
                existing.updateDetail(
                        incoming.getName(),
                        incoming.getSummary(),
                        incoming.getCoverImageId(),
                        incoming.getFirstReleaseDate() != null
                                ? incoming.getFirstReleaseDate().atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC)
                                : null,
                        incoming.getStoryline(),
                        incoming.getAggregatedRating(),
                        incoming.getFranchiseIgdbId(),
                        incoming.getFranchiseName()
                );
                existingGameIds.add(existing.getId());
                persistedMap.put(incoming.getIgdbId(), existing);
                updated++;
            }
        }

        // 3. 기존 게임의 join 엔티티 벌크 삭제 (8쿼리)
        if (!existingGameIds.isEmpty()) {
            gameGenreRepository.deleteByGameIdIn(existingGameIds);
            gamePlatformRepository.deleteByGameIdIn(existingGameIds);
            gameThemeRepository.deleteByGameIdIn(existingGameIds);
            gameKeywordRepository.deleteByGameIdIn(existingGameIds);
            gameGameModeRepository.deleteByGameIdIn(existingGameIds);
            gamePlayerPerspectiveRepository.deleteByGameIdIn(existingGameIds);
            gameCompanyRepository.deleteByGameIdIn(existingGameIds);
            gameExternalIdRepository.deleteByGameIdIn(existingGameIds);
        }

        // 4. 모든 게임(신규+기존)의 join 엔티티 수집
        List<GameGenre> allGenres = new ArrayList<>();
        List<GamePlatform> allPlatforms = new ArrayList<>();
        List<GameTheme> allThemes = new ArrayList<>();
        List<GameKeyword> allKeywords = new ArrayList<>();
        List<GameGameMode> allGameModes = new ArrayList<>();
        List<GamePlayerPerspective> allPlayerPerspectives = new ArrayList<>();
        List<GameCompany> allCompanies = new ArrayList<>();
        List<GameExternalId> allExternalIds = new ArrayList<>();

        for (GameBatchItem item : chunk) {
            Game game = persistedMap.get(item.getGame().getIgdbId());

            for (Genre g : item.getGenres())
                allGenres.add(GameGenre.createGameGenre(game, g));
            for (Platform p : item.getPlatforms())
                allPlatforms.add(GamePlatform.createGamePlatform(game, p));
            for (Theme t : item.getThemes())
                allThemes.add(GameTheme.createGameTheme(game, t));
            for (Keyword k : item.getKeywords())
                allKeywords.add(GameKeyword.createGameKeyword(game, k));
            for (GameMode gm : item.getGameModes())
                allGameModes.add(GameGameMode.createGameGameMode(game, gm));
            for (PlayerPerspective pp : item.getPlayerPerspectives())
                allPlayerPerspectives.add(GamePlayerPerspective.createGamePlayerPerspective(game, pp));
            for (GameBatchItem.CompanyRoleEntry entry : item.getCompanies())
                allCompanies.add(GameCompany.createGameCompany(game, entry.company(), entry.role()));
            for (GameBatchItem.ExternalIdEntry entry : item.getExternalIds())
                allExternalIds.add(GameExternalId.createGameExternalId(game, entry.platform(), entry.externalId()));
        }

        // 5. 벌크 저장 (8쿼리)
        if (!allGenres.isEmpty()) gameGenreRepository.saveAll(allGenres);
        if (!allPlatforms.isEmpty()) gamePlatformRepository.saveAll(allPlatforms);
        if (!allThemes.isEmpty()) gameThemeRepository.saveAll(allThemes);
        if (!allKeywords.isEmpty()) gameKeywordRepository.saveAll(allKeywords);
        if (!allGameModes.isEmpty()) gameGameModeRepository.saveAll(allGameModes);
        if (!allPlayerPerspectives.isEmpty()) gamePlayerPerspectiveRepository.saveAll(allPlayerPerspectives);
        if (!allCompanies.isEmpty()) gameCompanyRepository.saveAll(allCompanies);
        if (!allExternalIds.isEmpty()) gameExternalIdRepository.saveAll(allExternalIds);

        log.debug("게임 upsert: 신규 {}건, 갱신 {}건", created, updated);
    }
}
