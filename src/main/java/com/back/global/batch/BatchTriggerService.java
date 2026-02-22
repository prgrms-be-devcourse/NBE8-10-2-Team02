package com.back.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class BatchTriggerService {

    private final JobLauncher jobLauncher;
    private final Job igdbSyncJob;

    @Async
    public void runIgdbSyncAsync() {
        try {
            var params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("비동기 IGDB 동기화 Job 시작");
            var execution = jobLauncher.run(igdbSyncJob, params);
            log.info("IGDB 동기화 Job 완료: status={}", execution.getExitStatus().getExitCode());
        } catch (Exception e) {
            log.error("IGDB 동기화 Job 실행 실패", e);
        }
    }
}
