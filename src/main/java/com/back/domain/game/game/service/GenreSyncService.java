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
//    서버 켜질때 장르를 DB로 동기화

    private final IgdbService igdbService;
    private final GenreRepository genreRepository;

    public void syncGenres() {
        List<IgdbGenreDto> igdbGenres = igdbService.getGenres();
        // batch 이후에 값이 들어있다면 unique constraint 위반
        // findByIgdbIdIn()으로 기존 데이터 확인 후 없는거만 save()로 바꿨습니다.
        List<Long> igdbIds = igdbGenres.stream().map(IgdbGenreDto::id).toList();
        var existingIds = genreRepository.findByIgdbIdIn(igdbIds).stream()
                .map(Genre::getIgdbId)
                .collect(java.util.stream.Collectors.toSet());

        for (IgdbGenreDto dto : igdbGenres) {
            if (!existingIds.contains(dto.id())) {
                genreRepository.save(new Genre(dto.id(), dto.name()));
            }
        }
    }
}
