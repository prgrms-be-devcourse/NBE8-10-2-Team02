package com.back.global.vector;

import com.back.domain.game.game.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VectorDimensionConfig {

    @Bean
    public VectorDimensionMapper vectorDimensionMapper() {
        return new VectorDimensionMapper();
    }

    /**
     * Mapper를 DB 최신 데이터로 갱신하는 서비스.
     * 앱 시작 후 첫 배치에서 마스터 데이터가 채워진 뒤 호출되어야 한다.
     */
    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class VectorDimensionRefresher {

        private final VectorDimensionMapper vectorDimensionMapper;
        private final GenreRepository genreRepository;
        private final ThemeRepository themeRepository;
        private final GameKeywordRepository gameKeywordRepository;
        private final GameModeRepository gameModeRepository;
        private final PlayerPerspectiveRepository playerPerspectiveRepository;

        public void refresh() {
            Map<Long, Integer> genreMap = buildIndexMap(
                    genreRepository.findAll().stream().map(g -> g.getId()).toList(),
                    VectorDimensionMapper.GENRE_SIZE
            );

            Map<Long, Integer> themeMap = buildIndexMap(
                    themeRepository.findAll().stream().map(t -> t.getId()).toList(),
                    VectorDimensionMapper.THEME_SIZE
            );

            List<Long> topKeywordIds = gameKeywordRepository.findTop100KeywordIdsByFrequency();
            Map<Long, Integer> keywordMap = new HashMap<>();
            for (int i = 0; i < Math.min(topKeywordIds.size(), VectorDimensionMapper.KEYWORD_SIZE); i++) {
                keywordMap.put(topKeywordIds.get(i), i);
            }

            Map<Long, Integer> modeMap = buildIndexMap(
                    gameModeRepository.findAll().stream().map(m -> m.getId()).toList(),
                    VectorDimensionMapper.MODE_SIZE
            );

            Map<Long, Integer> perspectiveMap = buildIndexMap(
                    playerPerspectiveRepository.findAll().stream().map(p -> p.getId()).toList(),
                    VectorDimensionMapper.PERSPECTIVE_SIZE
            );

            vectorDimensionMapper.refresh(genreMap, themeMap, keywordMap, modeMap, perspectiveMap);

            log.info("VectorDimensionMapper 갱신 완료 - Genre:{}, Theme:{}, Keyword:{}, Mode:{}, Perspective:{}",
                    genreMap.size(), themeMap.size(), keywordMap.size(), modeMap.size(), perspectiveMap.size());
        }

        private Map<Long, Integer> buildIndexMap(List<Long> ids, int maxSize) {
            Map<Long, Integer> map = new HashMap<>();
            for (int i = 0; i < Math.min(ids.size(), maxSize); i++) {
                map.put(ids.get(i), i);
            }
            return map;
        }
    }
}
