package com.back.domain.member.memberGame.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberGameAddRequest(
        @NotBlank
        String platform,
        @NotNull
        double playtime,
        @NotNull
        boolean isFavorite,
        @NotNull
        int gameId
) {
}
