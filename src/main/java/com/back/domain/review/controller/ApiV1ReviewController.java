package com.back.domain.review.controller;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.service.GameService;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.memberGame.service.MemberGameService;
import com.back.domain.review.dto.ReviewDto;
import com.back.domain.review.dto.ReviewModifyRequest;
import com.back.domain.review.dto.ReviewWriteRequest;
import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "ApiV1ReviewController", description = "API 리뷰 컨트롤러")
public class ApiV1ReviewController {
    private final ReviewService reviewService;
    private final MemberService memberService;
    private final GameService gameService;
    private final Rq rq;
    private final MemberGameService memberGameService;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건 조회")
    public List<ReviewDto> getReviews() {
        List<Review> reviews = reviewService.findAll();
        return reviews
                .stream()
                .map(ReviewDto::new)
                .toList();
    }
    @GetMapping("/member/{memberId}")
    @Transactional(readOnly = true)
    @Operation(summary = "한 유저의 모든 리뷰 조회")
    public List<ReviewDto> getReviewsByMember(@PathVariable Long memberId) {
        List<Review> reviews = reviewService.findByAuthorId(memberId);
        return reviews
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    @GetMapping("/game/{gameId}")
    @Transactional(readOnly = true)
    @Operation(summary = "한 게임의 모든 리뷰 조회")
    public List<ReviewDto> getReviewsByGame(@PathVariable Long gameId) {
        List<Review> reviews = reviewService.findByGameId(gameId);
        return reviews
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public ReviewDto getReview(@PathVariable int id) {
        Review review = reviewService.findById(id).orElseThrow();//NoSuchElementException->globalExceptionHandler에서 처리됨
        return new ReviewDto(review);
    }


    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    public RsData<ReviewDto> write(@RequestBody ReviewWriteRequest reqBody) {
        Member actor = rq.getActor();
        Game game = gameService.findById(reqBody.gameId()).orElseThrow(() -> new ServiceException("404-1", "No Game"));
        Review review = reviewService.write(reqBody.title(), reqBody.content(), reqBody.rating(), actor, game);

        memberGameService.updateReview(actor.getId(), game.getId(), review);
        return new RsData<>(
                "201",
                "리뷰가 작성되었습니다.",
                new ReviewDto(review)
        );
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    public RsData<ReviewDto> modify(@PathVariable int id, @RequestBody ReviewModifyRequest reqBody) {
        Member actor = memberService.findById(rq.getActor().getId()).orElseThrow();
        Review review = reviewService.findById(id).orElseThrow();//Todo
        review.checkActorCanModify(actor);
        reviewService.modify(review, reqBody.title(), reqBody.content(), reqBody.rating());
        return new RsData<>(
                "201",
                "리뷰가 수정되었습니다.",
                new ReviewDto(review)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    public RsData<Void> delete(@PathVariable int id) {
        Member actor = rq.getActor();
        Review review = reviewService.findById(id).orElseThrow();//NoSuchElementException->globalExceptionHandler에서 처리됨
        review.checkActorCanDelete(actor);
        reviewService.delete(review);
        return new RsData<>(
                "200",
                "%d번 리뷰가 삭제되었습니다.".formatted(id)
        );
    }
}