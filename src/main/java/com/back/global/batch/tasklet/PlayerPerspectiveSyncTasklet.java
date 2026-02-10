package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.PlayerPerspective;
import com.back.domain.game.game.repository.PlayerPerspectiveRepository;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbPlayerPerspectiveDto;
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
public class PlayerPerspectiveSyncTasklet implements Tasklet {

    private final IgdbClient igdbClient;
    private final PlayerPerspectiveRepository playerPerspectiveRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbPlayerPerspectiveDto> igdbPlayerPerspectives = igdbClient.fetchPlayerPerspectives();
        log.info("IGDB에서 플레이어 시점 {}건 조회", igdbPlayerPerspectives.size());

        List<Long> igdbIds = igdbPlayerPerspectives.stream().map(IgdbPlayerPerspectiveDto::id).toList();
        Map<Long, PlayerPerspective> existingMap = playerPerspectiveRepository.findByIgdbIdIn(igdbIds).stream()
                .collect(Collectors.toMap(PlayerPerspective::getIgdbId, Function.identity()));

        int created = 0;
        for (IgdbPlayerPerspectiveDto dto : igdbPlayerPerspectives) {
            if (!existingMap.containsKey(dto.id())) {
                playerPerspectiveRepository.save(PlayerPerspective.createPlayerPerspective(dto.id(), dto.name()));
                created++;
            }
        }

        log.info("플레이어 시점 동기화 완료: 신규 {}건, 기존 {}건 스킵", created, igdbPlayerPerspectives.size() - created);
        return RepeatStatus.FINISHED;
    }
}
