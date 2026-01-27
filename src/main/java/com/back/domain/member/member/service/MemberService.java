package com.back.domain.member.member.service;

import com.back.domain.member.auth.service.AuthTokenService;
import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
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

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    @Transactional
    public Member changePassword(int memberId, String oldPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new ServiceException("401-2", "현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new ServiceException("400-2", "새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        String encoded = passwordEncoder.encode(newPassword);
        member.changePassword(encoded);

        return member;
    }

    public Member getMe(int memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원이 존재하지 않습니다."));
    }

    public void flush(){
        memberRepository.flush();
    }

    @Transactional
    public Member changeNickname(int memberId, String newNickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원이 존재하지 않습니다."));

        String nn = newNickname.trim();

        if (member.getNickname().equals(nn)) {
            throw new ServiceException("400-1", "현재 닉네임과 동일합니다.");
        }

        if (memberRepository.existsByNicknameAndIdNot(nn, memberId)) {
            throw new ServiceException("409-1", "이미 사용 중인 닉네임입니다.");
        }

        member.changeNickname(nn);

        return member;
    }
}
