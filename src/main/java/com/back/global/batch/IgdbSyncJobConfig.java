package com.back.global.batch;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.domain.game.game.repository.GenreRepository;
import com.back.domain.game.game.repository.PlatformRepository;
import com.back.global.batch.processor.IgdbGameProcessor;
import com.back.global.batch.reader.IgdbGamePageReader;
import com.back.global.batch.tasklet.GenreSyncTasklet;
import com.back.global.batch.tasklet.PlatformSyncTasklet;
import com.back.global.batch.writer.IgdbGameUpsertWriter;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.exception.IgdbApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job 하나와 Step 3개를 정의하는 설정 클래스
 * igdbSyncJob
 *     ├─ 1) genreSyncStep    (Tasklet)
 *     ├─ 2) platformSyncStep (Tasklet)
 *     └─ 3) gameSyncStep     (Chunk)
 */
@Configuration
@RequiredArgsConstructor
public class IgdbSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GenreSyncTasklet genreSyncTasklet;
    private final PlatformSyncTasklet platformSyncTasklet;
    private final IgdbClient igdbClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    @Bean
    public Job igdbSyncJob() {
        return new JobBuilder("igdbSyncJob", jobRepository)
                .start(genreSyncStep())
                .next(platformSyncStep())
                .next(gameSyncStep())
                .build();
    }

    @Bean
    public Step genreSyncStep() {
        return new StepBuilder("genreSyncStep", jobRepository)
                .tasklet(genreSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step platformSyncStep() {
        return new StepBuilder("platformSyncStep", jobRepository)
                .tasklet(platformSyncTasklet, transactionManager)
                .build();
    }

    /**
     * 빨간줄은 Spring Batch 6.0 정식 릴리즈 전까지는 대체 API가 아직 안정화되지 않았기 때문에, 지금은 그대로 두는 게 낫다.
     * 신경 쓰이면 IDE 설정에서 deprecated warning 수준을 낮출 수 있다.
     * IntelliJ: Settings > Editor > Inspections > Java > Deprecated API usage → Warning으로 변경
     */
    @Bean
    public Step gameSyncStep() {
        return new StepBuilder("gameSyncStep", jobRepository)
                .<IgdbGameDetailDto, Game>chunk(500, transactionManager)
                .reader(igdbGamePageReader())
                .processor(igdbGameProcessor())
                .writer(igdbGameUpsertWriter())
                .faultTolerant()
                .skip(IgdbApiException.class)
                .skipLimit(100)// 최대 100건까지 오류 허용, 101번째 실패 시 step이 실패, 이 다음에 retry를 뺀 것은 spring retry와 겹치기 때문
                .build();
    }

    @Bean
    @StepScope // step 실행 시 마다 새 인스턴스가 생성됨, Reader는 offset을 0부터 시작하고 Processor는 최신 장르/플랫폼을 캐싱해야 하므로 매번 새로 만듦
    public IgdbGamePageReader igdbGamePageReader() {
        // DB에 마지막 동기화 시점이 있으면 그 이후 변경분만 조회 (증분 동기화)
        // 없으면 null → 전체 동기화 (최초 실행)
        Long updatedAfterEpoch = gameRepository.findMaxLastFetchedAt()
                .map(instant -> instant.getEpochSecond())
                .orElse(null);

        return new IgdbGamePageReader(igdbClient, updatedAfterEpoch);
    }

    @Bean
    @StepScope
    public IgdbGameProcessor igdbGameProcessor() {
        return new IgdbGameProcessor(genreRepository, platformRepository);
    }

    @Bean
    // writer는 상태가 없어서 싱글턴으로 충분함
    public IgdbGameUpsertWriter igdbGameUpsertWriter() {
        return new IgdbGameUpsertWriter(gameRepository);
    }
}
