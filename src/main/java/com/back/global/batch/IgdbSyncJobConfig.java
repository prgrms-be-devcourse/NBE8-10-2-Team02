package com.back.global.batch;

import com.back.domain.game.game.repository.*;
import com.back.domain.game.recommendation.repository.GameVectorRepository;
import com.back.domain.game.recommendation.service.GameVectorService;
import com.back.global.batch.dto.GameBatchItem;
import com.back.global.batch.processor.IgdbGameProcessor;
import com.back.global.batch.reader.IgdbGamePageReader;
import com.back.global.batch.tasklet.*;
import com.back.global.batch.listener.DiscordBatchNotifier;
import com.back.global.batch.writer.IgdbGameUpsertWriter;
import com.back.global.igdb.BatchIgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import com.back.global.igdb.exception.IgdbApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job 하나와 Step 12개를 정의하는 설정 클래스
 * igdbSyncJob
 *     ├─  1) genreSyncStep              (Tasklet)
 *     ├─  2) platformSyncStep           (Tasklet)
 *     ├─  3) themeSyncStep              (Tasklet)
 *     ├─  4) gameModeSyncStep           (Tasklet)
 *     ├─  5) playerPerspectiveSyncStep  (Tasklet)
 *     ├─  6) keywordSyncStep            (Tasklet)
 *     ├─  7) companySyncStep            (Tasklet)
 *     ├─  8) vectorDimensionRefreshStep (Tasklet - 벡터 차원 매핑 1회 갱신)
 *     ├─  9) dropVectorIndexStep        (Tasklet - HNSW 인덱스 DROP)
 *     ├─ 10) gameSyncStep               (Chunk - 스테이징 테이블에 UPSERT)
 *     ├─ 11) applyVectorStagingStep     (Tasklet - staging → game 반영 + TRUNCATE, COMPLETED 시만)
 *     └─ 12) createVectorIndexStep      (Tasklet - HNSW 인덱스 CREATE, 항상 실행)
 */
@Configuration
@RequiredArgsConstructor
public class IgdbSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GenreSyncTasklet genreSyncTasklet;
    private final PlatformSyncTasklet platformSyncTasklet;
    private final ThemeSyncTasklet themeSyncTasklet;
    private final GameModeSyncTasklet gameModeSyncTasklet;
    private final PlayerPerspectiveSyncTasklet playerPerspectiveSyncTasklet;
    private final KeywordSyncTasklet keywordSyncTasklet;
    private final CompanySyncTasklet companySyncTasklet;
    private final DiscordBatchNotifier discordBatchNotifier;
    private final BatchIgdbClient igdbClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final ThemeRepository themeRepository;
    private final GameModeRepository gameModeRepository;
    private final PlayerPerspectiveRepository playerPerspectiveRepository;
    private final KeywordRepository keywordRepository;
    private final CompanyRepository companyRepository;
    private final GameGenreRepository gameGenreRepository;
    private final GamePlatformRepository gamePlatformRepository;
    private final GameThemeRepository gameThemeRepository;
    private final GameKeywordRepository gameKeywordRepository;
    private final GameGameModeRepository gameGameModeRepository;
    private final GamePlayerPerspectiveRepository gamePlayerPerspectiveRepository;
    private final GameCompanyRepository gameCompanyRepository;
    private final GameExternalIdRepository gameExternalIdRepository;
    private final GameVectorRepository gameVectorRepository;
    private final GameVectorService gameVectorService;
    private final com.back.global.vector.VectorDimensionConfig.VectorDimensionRefresher vectorDimensionRefresher;
    private final ApplyVectorStagingTasklet applyVectorStagingTasklet;
    private final DropVectorIndexTasklet dropVectorIndexTasklet;
    private final CreateVectorIndexTasklet createVectorIndexTasklet;

    @Bean
    public Job igdbSyncJob() {
        return new JobBuilder("igdbSyncJob", jobRepository)
                .listener(discordBatchNotifier)
                .start(genreSyncStep())
                .next(platformSyncStep())
                .next(themeSyncStep())
                .next(gameModeSyncStep())
                .next(playerPerspectiveSyncStep())
                .next(keywordSyncStep())
                .next(companySyncStep())
                .next(vectorDimensionRefreshStep())
                .next(dropVectorIndexStep())
                .next(gameSyncStep())
                    .on("COMPLETED").to(applyVectorStagingStep())
                    .next(createVectorIndexStep())
                .from(gameSyncStep())
                    .on("*").to(createVectorIndexStep())
                .end()
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

    @Bean
    public Step themeSyncStep() {
        return new StepBuilder("themeSyncStep", jobRepository)
                .tasklet(themeSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step gameModeSyncStep() {
        return new StepBuilder("gameModeSyncStep", jobRepository)
                .tasklet(gameModeSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step playerPerspectiveSyncStep() {
        return new StepBuilder("playerPerspectiveSyncStep", jobRepository)
                .tasklet(playerPerspectiveSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step keywordSyncStep() {
        return new StepBuilder("keywordSyncStep", jobRepository)
                .tasklet(keywordSyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step companySyncStep() {
        return new StepBuilder("companySyncStep", jobRepository)
                .tasklet(companySyncTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step vectorDimensionRefreshStep() {
        return new StepBuilder("vectorDimensionRefreshStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    vectorDimensionRefresher.refresh();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step dropVectorIndexStep() {
        return new StepBuilder("dropVectorIndexStep", jobRepository)
                .tasklet(dropVectorIndexTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step createVectorIndexStep() {
        return new StepBuilder("createVectorIndexStep", jobRepository)
                .tasklet(createVectorIndexTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step applyVectorStagingStep() {
        return new StepBuilder("applyVectorStagingStep", jobRepository)
                .tasklet(applyVectorStagingTasklet, transactionManager)
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
                .<IgdbGameDetailDto, GameBatchItem>chunk(500, transactionManager)
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
        return new IgdbGameProcessor(
                genreRepository,
                platformRepository,
                themeRepository,
                gameModeRepository,
                playerPerspectiveRepository,
                keywordRepository,
                companyRepository
        );
    }

    @Bean
    // writer는 상태가 없어서 싱글턴으로 충분함
    public IgdbGameUpsertWriter igdbGameUpsertWriter() {
        return new IgdbGameUpsertWriter(
                gameRepository,
                gameGenreRepository,
                gamePlatformRepository,
                gameThemeRepository,
                gameKeywordRepository,
                gameGameModeRepository,
                gamePlayerPerspectiveRepository,
                gameCompanyRepository,
                gameExternalIdRepository,
                gameVectorRepository,
                gameVectorService
        );
    }
}
