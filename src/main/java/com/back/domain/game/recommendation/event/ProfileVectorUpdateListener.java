package com.back.domain.game.recommendation.event;

import com.back.domain.game.recommendation.service.UserProfileVectorService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ProfileVectorUpdateListener {

    private static final long DEBOUNCE_SECONDS = 30;

    private final UserProfileVectorService userProfileVectorService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    public ProfileVectorUpdateListener(UserProfileVectorService userProfileVectorService) {
        this.userProfileVectorService = userProfileVectorService;
    }

    @EventListener
    public void handleProfileVectorUpdate(ProfileVectorUpdateEvent event) {
        int memberId = event.memberId();
        log.debug("프로필 벡터 갱신 이벤트 수신: memberId={}, reason={}", memberId, event.reason());

        pendingTasks.compute(memberId, (key, existingFuture) -> {
            if (existingFuture != null) {
                existingFuture.cancel(false);
                log.debug("디바운싱: 기존 예약 취소 memberId={}", memberId);
            }

            return scheduler.schedule(() -> {
                pendingTasks.remove(memberId);
                try {
                    userProfileVectorService.calculateAndSaveProfileVector(memberId);
                } catch (Exception e) {
                    log.warn("프로필 벡터 갱신 실패: memberId={}, error={}", memberId, e.getMessage());
                }
            }, DEBOUNCE_SECONDS, TimeUnit.SECONDS);
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
