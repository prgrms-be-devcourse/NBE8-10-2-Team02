package com.back.domain.game.game.dto;

public record GameVideoResponse(
        String videoId,
        String trailerEmbedUrl
) {
    private static final String VIDEO_URL_TEMPLATE =
            "https://www.youtube.com/embed/";

    public static GameVideoResponse from(String videoId) {
        String vId = (videoId == null) ? "" : videoId;
        String url = vId.isBlank() ? "" : VIDEO_URL_TEMPLATE + vId;
        return new GameVideoResponse(vId, url);
    }
}