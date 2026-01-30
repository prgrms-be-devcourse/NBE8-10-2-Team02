package com.back.global.search;

public class SearchNormalizer {
    // 검색어 정규화 (오타잡기)
    public static String normalize(String q) {
        if (q == null) return null;

        return q.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9가-힣]", "");
    }
}
