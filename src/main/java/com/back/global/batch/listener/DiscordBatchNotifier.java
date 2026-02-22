package com.back.global.batch.listener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DiscordBatchNotifier implements JobExecutionListener {

    private final String webhookUrl;
    private final RestClient restClient;

    public DiscordBatchNotifier(
            @Value("${discord.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restClient = RestClient.create();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (!isEnabled()) {
            return;
        }

        String message = formatEmbed(
                "IGDB 동기화 배치 시작",
                0x3498DB, // 파란색
                "시작 시간: " + formatTime(jobExecution.getStartTime())
        );

        send(message);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!isEnabled()) {
            return;
        }

        BatchStatus status = jobExecution.getStatus();
        boolean success = status == BatchStatus.COMPLETED;

        String duration = formatDuration(jobExecution.getStartTime(), jobExecution.getEndTime());
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();

        StringBuilder description = new StringBuilder();
        description.append("**상태**: ").append(status).append("\n");
        description.append("**소요 시간**: ").append(duration).append("\n");
        description.append("**시작**: ").append(formatTime(jobExecution.getStartTime())).append("\n");
        description.append("**종료**: ").append(formatTime(jobExecution.getEndTime())).append("\n\n");

        // Step별 결과 요약
        description.append("**Step 실행 결과**\n");
        for (StepExecution step : stepExecutions) {
            String icon = step.getStatus() == BatchStatus.COMPLETED ? "✅" : "❌";
            description.append(icon).append(" ")
                    .append(step.getStepName())
                    .append(" — R:").append(step.getReadCount())
                    .append(" W:").append(step.getWriteCount())
                    .append(" S:").append(step.getSkipCount())
                    .append("\n");
        }

        // 총 처리 건수
        long totalRead = stepExecutions.stream().mapToLong(StepExecution::getReadCount).sum();
        long totalWrite = stepExecutions.stream().mapToLong(StepExecution::getWriteCount).sum();
        long totalSkip = stepExecutions.stream().mapToLong(StepExecution::getSkipCount).sum();
        description.append("\n**총 처리**: Read ").append(totalRead)
                .append(" / Write ").append(totalWrite)
                .append(" / Skip ").append(totalSkip);

        // 실패 시 에러 메시지 추가
        if (!success) {
            description.append("\n\n**에러 정보**\n");
            for (StepExecution step : stepExecutions) {
                if (step.getStatus() != BatchStatus.COMPLETED && !step.getFailureExceptions().isEmpty()) {
                    String errors = step.getFailureExceptions().stream()
                            .map(Throwable::getMessage)
                            .collect(Collectors.joining("\n"));
                    description.append("❌ ").append(step.getStepName()).append(": ")
                            .append(truncate(errors, 500)).append("\n");
                }
            }
        }

        int color = success ? 0x2ECC71 : 0xE74C3C; // 초록 or 빨강
        String title = success ? "IGDB 동기화 배치 완료" : "IGDB 동기화 배치 실패";

        send(formatEmbed(title, color, description.toString()));
    }

    private boolean isEnabled() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    private void send(String jsonBody) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패: {}", e.getMessage());
        }
    }

    private String formatEmbed(String title, int color, String description) {
        // JSON 특수문자 이스케이프
        String escapedTitle = escapeJson(title);
        String escapedDesc = escapeJson(description);

        return """
                {"embeds":[{"title":"%s","description":"%s","color":%d}]}"""
                .formatted(escapedTitle, escapedDesc, color);
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "-";
        }
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return "%d시간 %d분 %d초".formatted(hours, minutes, seconds);
        }
        if (minutes > 0) {
            return "%d분 %d초".formatted(minutes, seconds);
        }
        return "%d초".formatted(seconds);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
