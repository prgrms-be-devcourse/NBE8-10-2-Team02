package com.back.domain.game.gameLike.repository;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.gameLike.entity.GameLike;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameLikeRepository extends JpaRepository<GameLike, Integer> {
                                                                                                                                  
      // 특정 회원이 특정 게임에 좋아요 눌렀는지 확인                                                                             
      Optional<GameLike> findByMemberAndGame(Member member, Game game);
                                                                                                                                  
      // 존재 여부만 확인 (성능 최적화)                                                                                           
      boolean existsByMemberAndGame(Member member, Game game);                                                                    
                                                                                                                                  
      // 특정 게임의 좋아요 수                                                                                                    
      long countByGame(Game game);
                                                                                                                                  
      // 특정 회원이 좋아요 누른 게임 목록                                                                                        
      @Query("SELECT gl.game FROM GameLike gl WHERE gl.member = :member ORDER BY gl.createdAt DESC")
      List<Game> findLikedGamesByMember(@Param("member") Member member);
                                                                                                                                  
      // igdbId로 좋아요 여부 확인 (조인 쿼리)
      @Query("SELECT CASE WHEN COUNT(gl) > 0 THEN true ELSE false END " +
             "FROM GameLike gl WHERE gl.member.id = :memberId AND gl.game.igdbId = :igdbId")
      boolean existsByMemberIdAndGameIgdbId(@Param("memberId") int memberId, @Param("igdbId") long igdbId);

      @Query("SELECT gl.game.id FROM GameLike gl WHERE gl.member.id = :memberId")
      List<Integer> findGameIdsByMemberId(@Param("memberId") int memberId);
  }