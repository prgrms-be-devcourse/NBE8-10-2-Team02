package com.back.domain.review.controller;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.service.GameService;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.dto.ReviewDto;
import com.back.domain.review.dto.ReviewModifyRequest;
import com.back.domain.review.dto.ReviewWriteRequest;
import com.back.domain.review.entity.Review;
import com.back.domain.review.service.ReviewService;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
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

    //private final rq;

    //TODO 나중에 (한 유저의 모든 리뷰 조회/한 게임의 모든 리뷰 조회) 도 추가
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

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public ReviewDto getReview(@PathVariable int id) {
        Review review = reviewService.findById(id).orElseThrow();//NoSuchElementException->globalExceptionHandler에서 처리됨
        return new ReviewDto(review);
    }
    //TODO Member가 해당 게임을 리뷰할 수 있는지 없는지 체크하는 기능이 어떤 형식으로든 있어야 함
    //동일 Member가 똑같은 계정으로 똑같은 게임에 리뷰 100개 다는 것 방지 위함.
    //예를 들면 그 유저가 아직 해당 게임에 대해 리뷰를 안 썼으면 리뷰 작성 버튼을 보여주고,
    //이미 리뷰를 썼으면 그 리뷰를 보여주며 수정 버튼 보여주는 등.
    //근데 이걸 MemberGameController에서 할 건지 아니면 ReviewController에서 할 건지, 프론트엔드 쪽 디자인을 정해야 함
    //또 Member가 자기 라이브러리에 있는 게임만 리뷰 가능하게 할지도 정해야 함.

    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    public RsData<ReviewDto> write(@RequestBody ReviewWriteRequest reqBody) {
        //Member actor = rq.getActor();
        //Game game = gameService.checkActorCanWriteReview(actor, reqBody.gameId());
        Member actor = memberService.findByEmail("john@gmail.com").get();
        Game game= gameService.findById(1).get();
        Review review = reviewService.write(reqBody.title(), reqBody.content(), reqBody.rating(), actor, game);
        return new RsData<>(
                "201",
                "리뷰가 작성되었습니다.",
                new ReviewDto(review)
        );
    }


    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    public RsData modify(@PathVariable int id, @RequestBody ReviewModifyRequest reqBody) {
        //Member actor = rq.getActor();
        Review review = reviewService.findById(id).orElseThrow();//NoSuchElementException->globalExceptionHandler에서 처리됨
        //review.checkActorCanModify(actor);
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
    public RsData<Void> delete(@PathVariable int id, @RequestBody ReviewModifyRequest reqBody) {
        //Member actor = rq.getActor();
        Review review = reviewService.findById(id).orElseThrow();//NoSuchElementException->globalExceptionHandler에서 처리됨
        //review.checkActorCanDelete(actor);
        reviewService.delete(review);
        return new RsData<>(
                "200",
                "%d번 리뷰가 삭제되었습니다.".formatted(id)
        );
    }
}