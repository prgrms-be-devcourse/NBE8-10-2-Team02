package com.back.global.vector;

import java.util.Map;

/**
 * 각 속성 엔티티의 DB id → 벡터 인덱스 매핑.
 * refresh()로 배치 후 최신 데이터로 갱신 가능.
 * 벡터 구조: [Genre(20)][Theme(30)][Keyword(100)][Mode(5)][Perspective(5)] = 160차원
 */
public class VectorDimensionMapper {

    public static final int GENRE_SIZE = 20;
    public static final int THEME_SIZE = 30;
    public static final int KEYWORD_SIZE = 100;
    public static final int MODE_SIZE = 5;
    public static final int PERSPECTIVE_SIZE = 5;
    public static final int TOTAL_DIMENSIONS = GENRE_SIZE + THEME_SIZE + KEYWORD_SIZE + MODE_SIZE + PERSPECTIVE_SIZE;

    public static final int GENRE_OFFSET = 0;
    public static final int THEME_OFFSET = GENRE_SIZE;
    public static final int KEYWORD_OFFSET = THEME_OFFSET + THEME_SIZE;
    public static final int MODE_OFFSET = KEYWORD_OFFSET + KEYWORD_SIZE;
    public static final int PERSPECTIVE_OFFSET = MODE_OFFSET + MODE_SIZE;

    private volatile Map<Long, Integer> genreIndexMap;
    private volatile Map<Long, Integer> themeIndexMap;
    private volatile Map<Long, Integer> keywordIndexMap;
    private volatile Map<Long, Integer> modeIndexMap;
    private volatile Map<Long, Integer> perspectiveIndexMap;

    public VectorDimensionMapper() {
        this.genreIndexMap = Map.of();
        this.themeIndexMap = Map.of();
        this.keywordIndexMap = Map.of();
        this.modeIndexMap = Map.of();
        this.perspectiveIndexMap = Map.of();
    }

    public void refresh(
            Map<Long, Integer> genreIndexMap,
            Map<Long, Integer> themeIndexMap,
            Map<Long, Integer> keywordIndexMap,
            Map<Long, Integer> modeIndexMap,
            Map<Long, Integer> perspectiveIndexMap
    ) {
        this.genreIndexMap = Map.copyOf(genreIndexMap);
        this.themeIndexMap = Map.copyOf(themeIndexMap);
        this.keywordIndexMap = Map.copyOf(keywordIndexMap);
        this.modeIndexMap = Map.copyOf(modeIndexMap);
        this.perspectiveIndexMap = Map.copyOf(perspectiveIndexMap);
    }

    public Integer getGenreIndex(Long genreId) {
        return genreIndexMap.get(genreId);
    }

    public Integer getThemeIndex(Long themeId) {
        return themeIndexMap.get(themeId);
    }

    public Integer getKeywordIndex(Long keywordId) {
        return keywordIndexMap.get(keywordId);
    }

    public Integer getModeIndex(Long modeId) {
        return modeIndexMap.get(modeId);
    }

    public Integer getPerspectiveIndex(Long perspectiveId) {
        return perspectiveIndexMap.get(perspectiveId);
    }

    public boolean isEmpty() {
        return genreIndexMap.isEmpty() && themeIndexMap.isEmpty()
                && keywordIndexMap.isEmpty() && modeIndexMap.isEmpty()
                && perspectiveIndexMap.isEmpty();
    }
}
