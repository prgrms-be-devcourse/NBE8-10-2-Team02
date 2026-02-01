package com.back.global.config;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.PopularGameCardDto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    @Bean
    public Cache<Long, AtomicLong> viewCountCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)  // 최대 1만 게임
                .build();
    }

    @Bean
    public Cache<String, List<PopularGameCardDto>> igdbPopularGamesCache() {
        return Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(30))  // IGDB 데이터는 24시간마다 갱신되므로 30분 캐시
                .build();
    }
}