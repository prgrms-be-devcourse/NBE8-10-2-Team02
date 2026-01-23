package com.back.domain.member.memberGame.dto;
import jakarta.validation.constraints.NotBlank;

public record MemberGameAddRequest(
        @NotBlank
        String platform,
        @NotBlank
        double playtime,
        @NotBlank
        boolean isFavorite,
        @NotBlank
        int gameId
) {
}
