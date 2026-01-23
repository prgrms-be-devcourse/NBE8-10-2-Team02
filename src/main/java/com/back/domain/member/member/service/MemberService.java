package com.back.domain.member.member.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.domain.member.memberGame.repository.MemberGameRepository;
import com.back.global.exception.ServiceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

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

        Member member = new Member(email, password, nickname);
        return memberRepository.save(member);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public MemberGame addToLibrary(String platform, double playtime, boolean isFavorite,  Member member, Game game) {
        return member.addMemberGame(platform, playtime, isFavorite, game);
    }


    public void flush(){
        memberRepository.flush();
    }
}