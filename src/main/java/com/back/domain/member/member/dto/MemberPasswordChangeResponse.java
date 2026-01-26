package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

public record MemberPasswordChangeResponse(
        int memberId,
        String email,
        String nickname
) {
    public MemberPasswordChangeResponse(Member member) {
        this(member.getId(), member.getEmail(), member.getNickname());
    }
}
