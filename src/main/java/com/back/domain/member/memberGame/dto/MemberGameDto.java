package com.back.domain.member.memberGame.dto;

import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.entity.MemberGame;

public record MemberGameDto(
        String platform,
        double playtime,
        boolean isFavorite,
        StatusEnum status,
        int gameId,
        String coverImageUrl,
        Integer reviewId,
        Double rating
){
    public MemberGameDto(MemberGame memberGame){
        this(
                memberGame.getPlatform(),
                memberGame.getPlaytime(),
                memberGame.isFavorite(),
                memberGame.getStatus(),
                memberGame.getGame().getId(),
                memberGame.getGame().getCoverImageId(),
                memberGame.getReview() != null ? memberGame.getReview().getId() : null,
                memberGame.getReview() != null ? memberGame.getReview().getRating() : null
        );
    }
}