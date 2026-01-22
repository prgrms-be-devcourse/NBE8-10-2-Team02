package com.back.domain.member.auth.controller;

import com.back.domain.member.auth.dto.AuthLoginRequest;
import com.back.domain.member.auth.dto.AuthLoginResponse;
import com.back.domain.member.auth.dto.AuthSignupRequest;
import com.back.domain.member.auth.dto.AuthSignupResponse;
import com.back.domain.member.auth.dto.CheckEmailResponse;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class ApiV1AuthController {

    private final MemberService memberService;
    private final Rq rq;

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public RsData<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest req) {
        Member member = memberService.login(req.email(), req.password());
        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "로그인 성공",
                new AuthLoginResponse(member, member.getApiKey(), accessToken)
        );
    }

    @PostMapping("/signup")
    @Transactional
    public RsData<AuthSignupResponse> signup(@Valid @RequestBody AuthSignupRequest req) {
        Member member = memberService.join(req.email(), req.password(), req.nickname());
        return new RsData<>("201-1", "회원가입 성공", new AuthSignupResponse(member));
    }

    @PostMapping("/logout")
    public RsData<Void> logout() {
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");
        return new RsData<>("200-1", "로그아웃 성공", null);
    }

    @GetMapping("/check-email")
    @Transactional(readOnly = true)
    public RsData<CheckEmailResponse> checkEmail(
            @RequestParam
            @NotBlank(message = "이메일은 필수 입력값입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {
        boolean available = !memberService.existsByEmail(email);

        return new RsData<>(
                "200-1",
                available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.",
                new CheckEmailResponse(available)
        );
    }
}
