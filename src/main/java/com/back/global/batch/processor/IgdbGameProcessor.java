package com.back.global.batch.processor;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.entity.Platform;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.domain.game.game.repository.PlatformRepository;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.dto.IgdbInvolvedCompanyDto;
import com.back.global.igdb.dto.IgdbPlatformDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class IgdbGameProcessor implements ItemProcessor<IgdbGameDetailDto, Game> {

    private final Map<Long, Genre> genreCache = new ConcurrentHashMap<>();
    private final Map<Long, Platform> platformCache = new ConcurrentHashMap<>();

    public IgdbGameProcessor(GenreRepository genreRepository, PlatformRepository platformRepository) {
        genreRepository.findAll().forEach(g -> genreCache.put(g.getIgdbId(), g));
        platformRepository.findAll().forEach(p -> platformCache.put(p.getIgdbId(), p));
        log.info("프로세서 캐시 초기화: 장르 {}건, 플랫폼 {}건", genreCache.size(), platformCache.size());
    }

    @Override
    public Game process(IgdbGameDetailDto dto) {
        if (dto.name() == null) {
            return null;
        }

        String summary = dto.summary() != null ? dto.summary() : "";
        if (summary.length() > 5000) {
            summary = summary.substring(0, 5000);
        }

        String coverImageId = dto.cover() != null ? dto.cover().imageId() : null;

        List<String> developers = new ArrayList<>();
        List<String> publishers = new ArrayList<>();
        if (dto.involvedCompanies() != null) {
            for (IgdbInvolvedCompanyDto ic : dto.involvedCompanies()) {
                if (ic.company() != null) {
                    if (ic.developer()) developers.add(ic.company().name());
                    if (ic.publisher()) publishers.add(ic.company().name());
                }
            }
        }

        Game game = Game.createGame(
                dto.id(),
                dto.name(),
                summary,
                developers,
                publishers,
                coverImageId,
                dto.firstReleaseDateEpochSeconds()
        );

        if (dto.genres() != null) {
            for (IgdbGenreDto g : dto.genres()) {
                Genre genre = genreCache.get(g.id());
                if (genre != null) {
                    game.addGenre(genre);
                }
            }
        }

        if (dto.platforms() != null) {
            for (IgdbPlatformDto p : dto.platforms()) {
                Platform platform = platformCache.get(p.id());
                if (platform != null) {
                    game.addPlatform(platform);
                }
            }
        }

        return game;
    }
}
