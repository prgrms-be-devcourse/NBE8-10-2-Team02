package com.back.domain.game.game.service;

import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.global.igdb.dto.IgdbGenreDto;
import com.back.global.igdb.service.IgdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreSyncService {
    private final IgdbService igdbService;
    private final GenreRepository genreRepository;

    public void syncGenres() {
        List<IgdbGenreDto> igdbGenres = igdbService.getGenres();

        for (IgdbGenreDto dto : igdbGenres) {
            genreRepository.save(
                    new Genre(dto.id(), dto.name())
            );
        }
    }
}
