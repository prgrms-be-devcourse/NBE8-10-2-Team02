package com.back.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Profile("dev")
public class BatchTriggerController {

    private final BatchTriggerService batchTriggerService;

    @PostMapping("/batch/igdb-sync")
    public Map<String, String> triggerIgdbSync() {
        batchTriggerService.runIgdbSyncAsync();
        return Map.of("status", "STARTED", "message", "Job이 백그라운드에서 실행 중입니다.");
    }
}