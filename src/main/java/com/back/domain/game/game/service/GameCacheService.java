package com.back.domain.game.game.service;

import com.back.domain.game.game.dto.GameDetailResponse;
import com.back.domain.game.game.dto.GameVideoResponse;
import com.back.domain.game.game.dto.PopularGameResponse;
import com.back.domain.game.game.dto.SimilarGameResponse;
import com.back.global.igdb.dto.PopularGameCardDto;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class GameCacheService {
    private final Cache<Long, GameDetailResponse> gameDetailCache;
    private final Cache<Long, GameVideoResponse> videoIdCache;
    private final Cache<Long, List<Long>> similarIdsCache;
    private final Cache<Long, List<SimilarGameResponse>> similarListCache;
    private final Cache<String, List<PopularGameResponse>> popularGamesCache;
    private final Cache<String, List<PopularGameCardDto>> igdbPopularGamesCache;
    private final Cache<Long, AtomicLong> viewCountCache;

    // GameDetail
    public GameDetailResponse getGameDetail(long igdbId) {
        return gameDetailCache.getIfPresent(igdbId);
    }
    public void putGameDetail(long igdbId, GameDetailResponse response) {
        gameDetailCache.put(igdbId, response);
    }

    // Video
    public GameVideoResponse getVideo(long igdbId) {
        return videoIdCache.getIfPresent(igdbId);
    }
    public void putVideo(long igdbId, GameVideoResponse response) {
        videoIdCache.put(igdbId, response);
    }

    // Similar IDs
    public List<Long> getSimilarIds(long igdbId) {
        return similarIdsCache.getIfPresent(igdbId);
    }
    public void putSimilarIds(long igdbId, List<Long> ids) {
        similarIdsCache.put(igdbId, ids);
    }

    // Similar List
    public List<SimilarGameResponse> getSimilarList(long igdbId) {
        return similarListCache.getIfPresent(igdbId);
    }
    public void putSimilarList(long igdbId, List<SimilarGameResponse> list) {
        similarListCache.put(igdbId, list);
    }

    // Popular Games
    public List<PopularGameResponse> getPopularGames(String key) {
        return popularGamesCache.getIfPresent(key);
    }
    public void putPopularGames(String key, List<PopularGameResponse> list) {
        popularGamesCache.put(key, list);
    }

    // IGDB Popular Games
    public List<PopularGameCardDto> getIgdbPopularGames(String key) {
        return igdbPopularGamesCache.getIfPresent(key);
    }
    public void putIgdbPopularGames(String key, List<PopularGameCardDto> list) {
        igdbPopularGamesCache.put(key, list);
    }

    // View Count
    public void incrementViewCount(long igdbId) {
        viewCountCache.get(igdbId, k -> new AtomicLong(0)).incrementAndGet();
    }
    public Map<Long, AtomicLong> getViewCountSnapshot() {
        return new HashMap<>(viewCountCache.asMap());
    }
}
