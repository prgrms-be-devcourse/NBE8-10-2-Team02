package com.back.domain.review.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    public Review write(String title, String content, double rating, Member member, Game game) {
        Review review = new Review(title, content, rating, member, game);
        return reviewRepository.save(review);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public void modify(Review review, String title, String content, double rating) {
        review.modify(title, content,rating);
    }

    public void delete(Review review) {
        reviewRepository.delete(review);
    }

}