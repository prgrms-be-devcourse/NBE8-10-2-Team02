package com.back.global.batch.reader;

import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemStreamException;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class IgdbGamePageReader implements ItemReader<IgdbGameDetailDto>, ItemStream {

    private static final String OFFSET_KEY = "igdb.game.offset";
    private static final int PAGE_SIZE = 500;

    private final IgdbClient igdbClient;

    private int currentOffset = 0;
    private final Queue<IgdbGameDetailDto> buffer = new ArrayDeque<>();
    private boolean exhausted = false; // 더이상 가져올 데이터가 없다는 플래그

    // ExecutionContext에 저장된 offset이 있으면 복원. 이전 실행이 중간에 실패했을 때 처음부터 다시 안하고 이어갈 수 있음
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(OFFSET_KEY)) {
            currentOffset = executionContext.getInt(OFFSET_KEY);
            log.info("재시작 감지 - offset {}부터 재개", currentOffset);
        }
    }

    // 매 chunk 완료 시 Spring Batch가 호출. 현재 offset을 ExecutionContext에 저장
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(OFFSET_KEY, currentOffset);
    }

    // buffer 비우기
    @Override
    public void close() throws ItemStreamException {
        buffer.clear();
    }

    /**
     * Spring Batch가 read() 호출
     *       ├─ buffer에 데이터 있음? → buffer.poll()로 1건 반환
     *       ├─ buffer 비었는데 exhausted=true? → null 반환 (= Step 종료)
     *       └─ buffer 비었고 아직 데이터 남음?
     *            ├─ igdbClient.fetchGamePage(offset, 500) 호출
     *            ├─ 결과가 빈 리스트 → exhausted=true, null 반환
     *            ├─ 결과가 500건 미만 → 마지막 페이지이므로 exhausted=true
     *            ├─ buffer에 전부 담음
     *            ├─ offset += 결과 건수
     *            └─ buffer.poll()로 1건 반환
     */
    @Override
    public IgdbGameDetailDto read() {
        if (!buffer.isEmpty()) {
            return buffer.poll();
        }

        if (exhausted) {
            return null;
        }

        List<IgdbGameDetailDto> page = igdbClient.fetchGamePage(currentOffset, PAGE_SIZE);
        log.info("IGDB 게임 페이지 조회: offset={}, 결과={}건", currentOffset, page.size());

        if (page.isEmpty()) {
            exhausted = true;
            return null;
        }

        currentOffset += page.size();
        buffer.addAll(page);

        if (page.size() < PAGE_SIZE) {
            exhausted = true;
        }

        return buffer.poll();
    }
}
