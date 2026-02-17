package com.back.global.batch.tasklet;

import com.back.domain.game.recommendation.repository.GameVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplyVectorStagingTasklet implements Tasklet {

    private final GameVectorRepository gameVectorRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        gameVectorRepository.applyStagingToGameAndTruncate();
        return RepeatStatus.FINISHED;
    }
}
