package com.back.domain.member.memberGame.dto;

import com.back.domain.member.memberGame.StatusEnum;
public record MemberGameUpdateRequest(
        String platform,
        Integer playtime,
        Boolean isFavorite,
        StatusEnum status // PLAYING, COMPLETED, DROPPED, ON_HOLD, PLAN_TO_PLAY
    )
{
}