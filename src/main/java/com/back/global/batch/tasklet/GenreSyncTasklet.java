package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGenreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreSyncTasklet implements Tasklet {

    private final IgdbClient igdbClient;
    private final GenreRepository genreRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // IGDB에서 장르 전체 조회
        List<IgdbGenreDto> igdbGenres = igdbClient.fetchGenres();
        log.info("IGDB에서 장르 {}건 조회", igdbGenres.size());

        // DB에 이미 있는 장르 중복insert방지
        List<Long> igdbIds = igdbGenres.stream().map(IgdbGenreDto::id).toList();
        Map<Long, Genre> existingMap = genreRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(Genre::getIgdbId, Function.identity()));

        int created = 0;
        for (IgdbGenreDto dto : igdbGenres) {
            if (!existingMap.containsKey(dto.id())) {
                genreRepository.save(Genre.createGenre(dto.id(), dto.name()));
                created++;
            }
        }

        log.info("장르 동기화 완료: 신규 {}건, 기존 {}건 스킵", created, igdbGenres.size() - created);
        return RepeatStatus.FINISHED;
    }
}
