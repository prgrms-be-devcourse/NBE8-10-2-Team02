package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.Platform;
import com.back.domain.game.game.repository.PlatformRepository;
import com.back.global.igdb.BatchIgdbClient;
import com.back.global.igdb.dto.IgdbPlatformDto;
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
public class PlatformSyncTasklet implements Tasklet {

    private final BatchIgdbClient igdbClient;
    private final PlatformRepository platformRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbPlatformDto> igdbPlatforms = igdbClient.fetchPlatforms();
        log.info("IGDB에서 플랫폼 {}건 조회", igdbPlatforms.size());

        List<Long> igdbIds = igdbPlatforms.stream().map(IgdbPlatformDto::id).toList();
        Map<Long, Platform> existingMap = platformRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(Platform::getIgdbId, Function.identity()));

        int created = 0;
        for (IgdbPlatformDto dto : igdbPlatforms) {
            if (!existingMap.containsKey(dto.id())) {
                platformRepository.save(Platform.createPlatform(dto.id(), dto.name()));
                created++;
            }
        }

        log.info("플랫폼 동기화 완료: 신규 {}건, 기존 {}건 스킵", created, igdbPlatforms.size() - created);
        return RepeatStatus.FINISHED;
    }
}
