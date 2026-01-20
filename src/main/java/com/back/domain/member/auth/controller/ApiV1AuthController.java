package com.back.domain.member.auth.controller;

import com.back.domain.member.auth.dto.AuthLoginRequest;
import com.back.domain.member.auth.dto.AuthLoginResponse;
import com.back.domain.member.auth.dto.AuthSignupRequest;
import com.back.domain.member.auth.dto.AuthSignupResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController {

    private final MemberService memberService;

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public RsData<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest req) {
        Member member = memberService.login(req.email(), req.password());
        return new RsData<>("200-1", "로그인 성공", new AuthLoginResponse(member));
    }

    @PostMapping("/signup")
    @Transactional
    public RsData<AuthSignupResponse> signup(@Valid @RequestBody AuthSignupRequest req) {
        Member member = memberService.join(req.email(), req.password(), req.nickname());
        return new RsData<>("201-1", "회원가입 성공", new AuthSignupResponse(member));
    }
}
