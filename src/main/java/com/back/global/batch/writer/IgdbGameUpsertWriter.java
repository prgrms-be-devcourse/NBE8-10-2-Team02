package com.back.global.batch.writer;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.GameGenre;
import com.back.domain.game.game.entity.GamePlatform;
import com.back.domain.game.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IgdbGameUpsertWriter implements ItemWriter<Game> {

    private final GameRepository gameRepository;

    @Override
    public void write(Chunk<? extends Game> chunk) {
        List<Long> igdbIds = chunk.getItems().stream().map(Game::getIgdbId).toList();
        Map<Long, Game> existingMap = gameRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(Game::getIgdbId, Function.identity()));

        int created = 0;
        int updated = 0;

        for (Game incoming : chunk) {
            Game existing = existingMap.get(incoming.getIgdbId());

            if (existing == null) {
                gameRepository.save(incoming);
                created++;
            } else {
                existing.updateDetail(
                        incoming.getName(),
                        incoming.getSummary(),
                        incoming.getCoverImageId(),
                        incoming.getFirstReleaseDate() != null
                                ? incoming.getFirstReleaseDate().atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC)
                                : null
                );
                existing.updateCompanies(incoming.getDevelopers(), incoming.getPublishers());

                existing.getGameGenres().clear();
                for (GameGenre gg : incoming.getGameGenres()) {
                    existing.addGenre(gg.getGenre());
                }

                existing.getGamePlatforms().clear();
                for (GamePlatform gp : incoming.getGamePlatforms()) {
                    existing.addPlatform(gp.getPlatform());
                }

                updated++;
            }
        }

        log.debug("게임 upsert: 신규 {}건, 갱신 {}건", created, updated);
    }
}
