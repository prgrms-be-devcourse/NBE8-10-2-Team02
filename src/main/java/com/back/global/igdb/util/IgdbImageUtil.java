package com.back.global.igdb.util;

public class IgdbImageUtil {
    private static final String BASE =
            "https://images.igdb.com/igdb/image/upload/";

    public static String cover(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return null;
        }
        return BASE + "t_cover_big/" + imageId + ".jpg";
    }
}
