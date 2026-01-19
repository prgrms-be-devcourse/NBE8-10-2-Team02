package com.back.domain.member.auth.dto;

import com.back.domain.member.member.entity.Member;

public record AuthLoginResponse(
        int memberId,
        String email,
        String nickname
) {
    public AuthLoginResponse(Member member) {
        this(member.getId(), member.getEmail(), member.getNickname());
    }
}
