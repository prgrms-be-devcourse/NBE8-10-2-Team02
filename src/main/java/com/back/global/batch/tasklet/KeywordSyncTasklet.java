package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.Keyword;
import com.back.domain.game.game.repository.KeywordRepository;
import com.back.global.igdb.BatchIgdbClient;
import com.back.global.igdb.dto.IgdbKeywordDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordSyncTasklet implements Tasklet {

    private final BatchIgdbClient igdbClient;
    private final KeywordRepository keywordRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbKeywordDto> igdbKeywords = igdbClient.fetchKeywords();
        log.info("IGDB에서 키워드 {}건 조회", igdbKeywords.size());

        Map<Long, Keyword> existingMap = keywordRepository.findAll().stream()
                .collect(Collectors.toMap(Keyword::getIgdbId, Function.identity()));

        List<Keyword> toSave = new ArrayList<>();
        for (IgdbKeywordDto dto : igdbKeywords) {
            if (!existingMap.containsKey(dto.id())) {
                toSave.add(Keyword.createKeyword(dto.id(), dto.name()));
            }
        }

        if (!toSave.isEmpty()) {
            keywordRepository.saveAll(toSave);
        }

        log.info("키워드 동기화 완료: 신규 {}건, 기존 {}건 스킵", toSave.size(), igdbKeywords.size() - toSave.size());
        return RepeatStatus.FINISHED;
    }
}
