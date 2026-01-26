package com.back.domain.game.platform;

import java.util.List;
import java.util.Map;

public class PlatformGroup {
    /**
     * 대표 플랫폼 코드
     */
    public static final Map<String, List<Long>> PLATFORM_MAP = Map.of(
            "PS", List.of(48L, 167L, 9L, 8L, 7L),
            "XBOX", List.of(49L, 169L, 11L),
            "NINTENDO", List.of(130L, 41L, 37L, 18L, 33L),
            "PC", List.of(6L),
            "MOBILE", List.of(34L, 39L),
            "VR", List.of(162L, 163L)
    );

    /**
     * 드롭다운용
     */
    public static final Map<String, String> DISPLAY_NAME = Map.of(
            "PS", "PlayStation",
            "XBOX", "Xbox",
            "NINTENDO", "Nintendo",
            "PC", "PC",
            "MOBILE", "Mobile",
            "VR", "VR"
    );
}
