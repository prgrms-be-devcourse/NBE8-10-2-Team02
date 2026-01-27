package com.back.domain.review.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.memberGame.repository.MemberGameRepository;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MemberGameRepository memberGameRepository;

    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    public boolean existsByMemberIdGameId(Member member, Game game) {
        return reviewRepository.findByAuthorAndGame(member, game).isPresent();
    }
    public Review write(String title, String content, double rating, Member member, Game game) {
        // Check if user owns the game in their library
        memberGameRepository.findByMemberIdAndGameId(member.getId(), game.getId())
                .orElseThrow(() -> new ServiceException("403-3", "라이브러리에 없는 게임은 리뷰를 작성할 수 없습니다."));

        // Check for duplicate review
        Optional<Review> existingReview = reviewRepository.findByAuthorAndGame(member, game);
        if (existingReview.isPresent()) {
            throw new ServiceException("400-1", "이미 해당 게임에 대한 리뷰를 작성하셨습니다.");
        }

        Review review = new Review(title, content, rating, member, game);
        return reviewRepository.save(review);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Page<Review> findAll(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public List<Review> findByAuthorId(Long authorId) {
        return reviewRepository.findByAuthorId(authorId);
    }

    public Page<Review> findByAuthorId(Long authorId, Pageable pageable) {
        return reviewRepository.findByAuthorId(authorId, pageable);
    }

    public List<Review> findByGameId(Long gameId) {
        return reviewRepository.findByGameId(gameId);
    }

    public Page<Review> findByGameId(Long gameId, Pageable pageable) {
        return reviewRepository.findByGameId(gameId, pageable);
    }

    public void modify(Review review, String title, String content, double rating) {
        review.modify(title, content,rating);
    }

    public void delete(Review review) {
        // Clear the review reference in MemberGame if it exists
        memberGameRepository.findByMemberIdAndGameId(review.getAuthor().getId(), review.getGame().getId())
                .ifPresent(memberGame -> memberGame.setReview(null));

        reviewRepository.delete(review);
    }

}