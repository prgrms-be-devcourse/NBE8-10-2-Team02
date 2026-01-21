package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

public record MemberMeResponse(
        int id,
        String email,
        String nickname
) {
    public MemberMeResponse(Member member) {
        this(member.getId(), member.getEmail(), member.getNickname());
    }
}
