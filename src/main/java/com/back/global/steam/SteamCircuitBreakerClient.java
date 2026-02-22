package com.back.global.steam;

import com.back.global.steam.dto.SteamGameDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SteamCircuitBreakerClient {

    private final SteamClient steamClient;

    @CircuitBreaker(name = "steam", fallbackMethod = "getOwnedGamesFallback")
    @Bulkhead(name = "steam", fallbackMethod = "getOwnedGamesFallback")
    public List<SteamGameDto> getOwnedGames(String steamId) {
        return steamClient.getOwnedGames(steamId);
    }

    private List<SteamGameDto> getOwnedGamesFallback(String steamId, Throwable t) {
        log.warn("Steam fallback – getOwnedGames(steamId={}), cause: {}", steamId, t.getMessage());
        return List.of();
    }
}
