package com.back.global.igdb.dto;

import java.util.List;
import java.util.Objects;

public record PopularGameCardDto(
        long id,
        String name,
        String coverImageId,
        List<String> genres,
        double score
    ) {
        public static PopularGameCardDto from(GameRow g, double score) {
            List<String> genreNames = g.genres() == null ? List.of()
                : g.genres().stream().map(IgdbGenreDto::name).filter(Objects::nonNull).toList();

            String coverId = (g.cover() == null) ? null : g.cover().imageId();

            return new PopularGameCardDto(
                g.id(),
                g.name(),
                coverId,
                genreNames,
                score
            );
        }
    }