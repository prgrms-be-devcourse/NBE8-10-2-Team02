package com.back.global.cache;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {
    @Bean
    public Cache<Long, GameDetailResponse> gameDetailCache() {
        return Caffeine.newBuilder()
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofHours(12))
                .build();
    }

    @Bean
    public Cache<Long, GameVideoResponse> videoIdCache() {
        return Caffeine.newBuilder()
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofDays(10))
                .build();
    }

    @Bean
    public Cache<Long, List<Long>> similarIdsCache() {
        return Caffeine.newBuilder()
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofHours(24))
                .build();
    }

    @Bean
    public Cache<Long, List<SimilarGameResponse>> similarListCache() {
        return Caffeine.newBuilder()
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofHours(24))
                .build();
    }
}