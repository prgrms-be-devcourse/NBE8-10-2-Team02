package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.CheckNicknameResponse;
import com.back.domain.member.member.dto.MemberMeResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
public class ApiV1MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/check-nickname")
    @Transactional(readOnly = true)
    public RsData<CheckNicknameResponse> checkNickname(
            @RequestParam
            @NotBlank(message = "닉네임은 필수 입력값입니다.")
            @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
            String nickname
    ) {
        boolean available = !memberRepository.existsByNickname(nickname);

        return new RsData<>(
                "200-1",
                available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                new CheckNicknameResponse(available)
        );
    }

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
