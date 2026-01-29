package com.back.domain.game.platform;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Get the default (first) platformId for a group name
     * @param groupName the platform group name (e.g., "PC", "PS")
     * @return the default platformId, or null if not found
     */
    public static Long getDefaultPlatformId(String groupName) {
        if (groupName == null) return null;
        List<Long> ids = PLATFORM_MAP.get(groupName.toUpperCase());
        return (ids != null && !ids.isEmpty()) ? ids.get(0) : null;
    }

    /**
     * Get all platformIds for a group name (for filtering)
     * @param groupName the platform group name (e.g., "PC", "PS")
     * @return list of platformIds, or empty list if not found
     */
    public static List<Long> getPlatformIds(String groupName) {
        if (groupName == null) return Collections.emptyList();
        List<Long> ids = PLATFORM_MAP.get(groupName.toUpperCase());
        return ids != null ? ids : Collections.emptyList();
    }

    /**
     * Get the group name for a platformId (for display)
     * @param platformId the platform ID
     * @return the group name, or null if not found
     */
    public static String getGroupName(Long platformId) {
        if (platformId == null) return null;
        for (Map.Entry<String, List<Long>> entry : PLATFORM_MAP.entrySet()) {
            if (entry.getValue().contains(platformId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all available group names
     * @return set of group names
     */
    public static Set<String> getAllGroupNames() {
        return PLATFORM_MAP.keySet();
    }
}
