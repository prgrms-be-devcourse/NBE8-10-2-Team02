package com.back.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobOperator jobOperator;
    private final Job igdbSyncJob;

    @Scheduled(cron = "0 0 3 * * MON")
    public void runIgdbSyncJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("IGDB 동기화 Job 시작");
            jobOperator.run(igdbSyncJob, params);
            log.info("IGDB 동기화 Job 완료");
        } catch (Exception e) {
            log.error("IGDB 동기화 Job 실행 실패", e);
        }
    }
}
