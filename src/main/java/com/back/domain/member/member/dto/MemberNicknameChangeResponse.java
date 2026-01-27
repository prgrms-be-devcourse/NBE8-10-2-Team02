package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

public record MemberNicknameChangeResponse(
        int id,
        String email,
        String nickname
) {
    public MemberNicknameChangeResponse(Member member) {
        this(member.getId(), member.getEmail(), member.getNickname());
    }
}
