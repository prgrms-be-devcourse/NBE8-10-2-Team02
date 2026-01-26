package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GenreResponse;
import com.back.domain.game.game.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreResponse> getGenres() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreResponse(
                        g.getIgdbId(),
                        g.getName()
                ))
                .toList();
    }
}

