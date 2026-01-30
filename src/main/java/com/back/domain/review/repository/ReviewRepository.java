package com.back.domain.review.repository;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByAuthorAndGame(Member author, Game game);
    List<Review> findByAuthorId(Long authorId);
    List<Review> findByGameId(Long gameId);

    Page<Review> findAll(Pageable pageable);
    Page<Review> findByAuthorId(Long authorId, Pageable pageable);
    Page<Review> findByGameId(Long gameId, Pageable pageable);
}