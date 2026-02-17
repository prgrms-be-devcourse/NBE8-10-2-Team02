package com.back.global.batch.tasklet;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateVectorIndexTasklet implements Tasklet {

    private final EntityManager entityManager;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("[Batch] Creating HNSW index: {}", VectorBatchSql.VECTOR_INDEX_NAME);
        entityManager.createNativeQuery(VectorBatchSql.CREATE_VECTOR_INDEX).executeUpdate();
        log.info("[Batch] Created HNSW index: {}", VectorBatchSql.VECTOR_INDEX_NAME);
        return RepeatStatus.FINISHED;
    }
}
