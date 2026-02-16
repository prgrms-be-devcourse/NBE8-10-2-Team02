package com.back.domain.game.recommendation.repository;

import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberVectorRepository extends JpaRepository<Member, Integer> {

    @Modifying
    @Query(value = "UPDATE member SET profile_vector = cast(:vector as vector) WHERE id = :memberId",
            nativeQuery = true)
    void updateProfileVector(@Param("memberId") int memberId, @Param("vector") String vector);

    @Query(value = "SELECT cast(profile_vector as text) FROM member WHERE id = :memberId",
            nativeQuery = true)
    String getProfileVectorString(@Param("memberId") int memberId);
}
