package com.back.global.batch.tasklet;

import com.back.domain.game.game.entity.Company;
import com.back.domain.game.game.repository.CompanyRepository;
import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbCompanyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySyncTasklet implements Tasklet {

    private final IgdbClient igdbClient;
    private final CompanyRepository companyRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<IgdbCompanyDto> igdbCompanies = igdbClient.fetchCompanies();
        log.info("IGDB에서 회사 {}건 조회", igdbCompanies.size());

        Map<Long, Company> existingMap = companyRepository.findAll().stream()
                .collect(Collectors.toMap(Company::getIgdbId, Function.identity()));

        List<Company> toSave = new ArrayList<>();
        for (IgdbCompanyDto dto : igdbCompanies) {
            if (!existingMap.containsKey(dto.id())) {
                toSave.add(Company.createCompany(dto.id(), dto.name()));
            }
        }

        if (!toSave.isEmpty()) {
            companyRepository.saveAll(toSave);
        }

        log.info("회사 동기화 완료: 신규 {}건, 기존 {}건 스킵", toSave.size(), igdbCompanies.size() - toSave.size());
        return RepeatStatus.FINISHED;
    }
}
