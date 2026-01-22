package com.back.domain.member.member.service;

import com.back.domain.member.auth.service.AuthTokenService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthTokenService authTokenService;

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Member join(String email, String password, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new ServiceException("409-1", "이미 존재하는 이메일입니다.");
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new ServiceException("409-2", "이미 존재하는 닉네임입니다.");
        }

        String encoded = passwordEncoder.encode(password);
        Member member = new Member(email, encoded, nickname);
        return memberRepository.save(member);
    }

    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("401-1", "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException("401-1", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
