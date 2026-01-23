package com.back.domain.member.memberGame.repository;

import com.back.domain.member.memberGame.entity.MemberGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberGameRepository extends JpaRepository<MemberGame, Integer> {
}