package com.back.domain.member.auth.dto;

import com.back.domain.member.member.entity.Member;

public record AuthSignupResponse(
        int memberId,
        String email,
        String nickname
) {
    public AuthSignupResponse(Member member) {
        this(member.getId(), member.getEmail(), member.getNickname());
    }
}
