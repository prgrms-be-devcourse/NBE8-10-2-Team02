package com.back.global.batch.reader;

import com.back.global.igdb.IgdbClient;
import com.back.global.igdb.dto.IgdbGameDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemStreamException;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class IgdbGamePageReader implements ItemReader<IgdbGameDetailDto>, ItemStream {

    private static final String OFFSET_KEY = "igdb.game.offset";
    private static final int PAGE_SIZE = 500;

    private final IgdbClient igdbClient;
    private final Long updatedAfterEpoch;

    private int currentOffset = 0;
    private final Queue<IgdbGameDetailDto> buffer = new ArrayDeque<>();
    private boolean exhausted = false;

    public IgdbGamePageReader(IgdbClient igdbClient, Long updatedAfterEpoch) {
        this.igdbClient = igdbClient;
        this.updatedAfterEpoch = updatedAfterEpoch;

        if (updatedAfterEpoch != null) {
            log.info("증분 동기화 모드: updated_at > {} 이후 변경된 게임만 조회", updatedAfterEpoch);
        } else {
            log.info("전체 동기화 모드: 모든 게임 조회");
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(OFFSET_KEY)) {
            currentOffset = executionContext.getInt(OFFSET_KEY);
            log.info("재시작 감지 - offset {}부터 재개", currentOffset);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(OFFSET_KEY, currentOffset);
    }

    @Override
    public void close() throws ItemStreamException {
        buffer.clear();
    }

    /**
     * Spring Batch가 read() 호출
     *       ├─ buffer에 데이터 있음? → buffer.poll()로 1건 반환
     *       ├─ buffer 비었는데 exhausted=true? → null 반환 (= Step 종료)
     *       └─ buffer 비었고 아직 데이터 남음?
     *            ├─ igdbClient.fetchGamePage(offset, 500, updatedAfterEpoch) 호출
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

        List<IgdbGameDetailDto> page = igdbClient.fetchGamePage(currentOffset, PAGE_SIZE, updatedAfterEpoch);
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
