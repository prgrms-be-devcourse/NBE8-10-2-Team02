package com.back.domain.game.game.init;

import com.back.domain.game.game.service.GenreSyncService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreInitializer {
    private final GenreSyncService genreSyncService;

    @PostConstruct
    public void init() {
        genreSyncService.syncGenres();
    }
}
