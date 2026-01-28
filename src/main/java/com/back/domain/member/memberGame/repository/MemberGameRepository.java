package com.back.domain.member.memberGame.repository;

import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.entity.MemberGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberGameRepository extends JpaRepository<MemberGame, Integer> {
    Optional<MemberGame> findByMemberIdAndGameId(int memberId, int gameId);

    Page<MemberGame> findByMemberId(int memberId, Pageable pageable);

    Page<MemberGame> findByMemberIdAndStatus(int memberId, StatusEnum status, Pageable pageable);

    Page<MemberGame> findByMemberIdAndPlatform(int memberId, String platform, Pageable pageable);

    Page<MemberGame> findByMemberIdAndStatusAndPlatform(int memberId, StatusEnum status, String platform, Pageable pageable);
}