package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.GameMode;
import com.back.domain.game.game.repository.GameModeRepository;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameModeDto;
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
public class GameModeSyncTasklet implements Tasklet {

    private final IgdbClient igdbClient;
    private final GameModeRepository gameModeRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbGameModeDto> igdbGameModes = igdbClient.fetchGameModes();
        log.info("IGDB에서 게임 모드 {}건 조회", igdbGameModes.size());

        List<Long> igdbIds = igdbGameModes.stream().map(IgdbGameModeDto::id).toList();
        Map<Long, GameMode> existingMap = gameModeRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(GameMode::getIgdbId, Function.identity()));

        int created = 0;
        for (IgdbGameModeDto dto : igdbGameModes) {
            if (!existingMap.containsKey(dto.id())) {
                gameModeRepository.save(GameMode.createGameMode(dto.id(), dto.name()));
                created++;
            }
        }

        log.info("게임 모드 동기화 완료: 신규 {}건, 기존 {}건 스킵", created, igdbGameModes.size() - created);
        return RepeatStatus.FINISHED;
    }
}
