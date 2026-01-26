package com.back.domain.game.game.dto;

import com.back.domain.game.game.entity.Genre;

public record GenreResponse(
        long id,      // IGDB id
        String name
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getIgdbId(),  //IGDB id
                genre.getName()
        );
    }
}
