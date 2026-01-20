package com.back.domain.member.auth.dto;

import com.back.domain.member.member.entity.Member;

public record AuthLoginResponse(
        int memberId,
        String email,
        String nickname,
        String apiKey,
        String accessToken
) {
    public AuthLoginResponse(Member member, String apiKey, String accessToken) {
        this(member.getId(), member.getEmail(), member.getNickname(), apiKey, accessToken);
    }
}
