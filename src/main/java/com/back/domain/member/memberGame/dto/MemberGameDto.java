package com.back.domain.member.memberGame.dto;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.memberGame.entity.MemberGame;

public record MemberGameDto(
        String platform,
        double playtime,
        boolean isFavorite,
        Game game
){
    public MemberGameDto(MemberGame memberGame){
        this(
                memberGame.getPlatform(),
                memberGame.getPlaytime(),
                memberGame.isFavorite(),
                memberGame.getGame()
        );
    }
}