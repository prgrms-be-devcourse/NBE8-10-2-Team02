package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.Theme;
import com.back.domain.game.game.repository.ThemeRepository;
import com.back.global.igdb.BatchIgdbClient;
import com.back.global.igdb.dto.IgdbThemeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThemeSyncTasklet implements Tasklet {

    private final BatchIgdbClient igdbClient;
    private final ThemeRepository themeRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbThemeDto> igdbThemes = igdbClient.fetchThemes();
        log.info("IGDB에서 테마 {}건 조회", igdbThemes.size());

        List<Long> igdbIds = igdbThemes.stream().map(IgdbThemeDto::id).toList();
        Map<Long, Theme> existingMap = themeRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(Theme::getIgdbId, Function.identity()));

        int created = 0;
        for (IgdbThemeDto dto : igdbThemes) {
            if (!existingMap.containsKey(dto.id())) {
                themeRepository.save(Theme.createTheme(dto.id(), dto.name()));
                created++;
            }
        }

        log.info("테마 동기화 완료: 신규 {}건, 기존 {}건 스킵", created, igdbThemes.size() - created);
        return RepeatStatus.FINISHED;
    }
}
