package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, int id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByApiKey(String apiKey);
}