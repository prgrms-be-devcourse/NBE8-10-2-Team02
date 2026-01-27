package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.CheckNicknameResponse;
import com.back.domain.member.member.dto.MemberMeResponse;
import com.back.domain.member.member.dto.MemberPasswordChangeRequest;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
public class ApiV1MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/check-nickname")
    @Transactional(readOnly = true)
    public RsData<CheckNicknameResponse> checkNickname(
            @RequestParam
            @NotBlank(message = "닉네임은 필수 입력값입니다.")
            @Size(min = 2, max = 30, message = "닉네임은 2~30자여야 합니다.")
            String nickname
    ) {
        boolean available = !memberService.existsByNickname(nickname);

        return new RsData<>(
                "200-1",
                available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                new CheckNicknameResponse(available)
        );
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public RsData<MemberMeResponse> me() {
        Member actor = rq.getActor();
        if (actor == null) throw new ServiceException("401-1", "로그인 후 이용해주세요.");

        Member member = memberService.getMe(actor.getId());
        return new RsData<>("200-1", "내 정보 조회 성공", new MemberMeResponse(member));
    }

    @PutMapping("/me/password")
    @Transactional
    public RsData<Void> changePassword(
            @Valid @RequestBody MemberPasswordChangeRequest req
    ) {
        Member actor = rq.getActor();
        if (actor == null) throw new ServiceException("401-1", "로그인 후 이용해주세요.");

        memberService.changePassword(actor.getId(), req.oldPassword(), req.newPassword());

        return new RsData<>("200-1", "비밀번호 변경 성공", null);
    }
}
