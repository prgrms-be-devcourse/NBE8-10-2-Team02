package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberMeResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public RsData<MemberMeResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ServiceException("401-1", "로그인 후 이용해주세요.");
        }

        var principal = auth.getPrincipal();

        int memberId;
        try {
            memberId = (int) principal.getClass().getMethod("getId").invoke(principal);
        } catch (Exception e) {
            throw new ServiceException("401-1", "인증 정보가 올바르지 않습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원이 존재하지 않습니다."));

        return new RsData<>("200-1", "내 정보 조회 성공", new MemberMeResponse(member));
    }
}
