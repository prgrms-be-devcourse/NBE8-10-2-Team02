package com.back.global.batch.tasklet;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropVectorIndexTasklet implements Tasklet {

    private final EntityManager entityManager;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("[Batch] Dropping HNSW index: {}", VectorBatchSql.VECTOR_INDEX_NAME);
        entityManager.createNativeQuery(VectorBatchSql.DROP_VECTOR_INDEX).executeUpdate();
        log.info("[Batch] Dropped HNSW index: {}", VectorBatchSql.VECTOR_INDEX_NAME);
        return RepeatStatus.FINISHED;
    }
}
