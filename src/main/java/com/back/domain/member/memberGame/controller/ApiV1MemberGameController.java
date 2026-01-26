package com.back.domain.member.memberGame.controller;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.service.GameService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.memberGame.dto.MemberGameAddRequest;
import com.back.domain.member.memberGame.dto.MemberGameDto;
import com.back.domain.member.memberGame.entity.MemberGame;

import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/members/{memberId}/library")
@RequiredArgsConstructor
public class ApiV1MemberGameController {
    private final GameService gameService;
    private final MemberService memberService;

    //TODO Library 조회
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건 조회(회원별)")
    public List<MemberGame> viewLibrary(@PathVariable("memberId") int memberId){
        //T회원 확인하는 코드 넣기
        List<MemberGame> memberGames = new ArrayList<>();
        return memberGames;
    }

    //TODO memberGameDetail (+GameDetailDto) (리뷰작성여부)까지 보여주는 API
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회")
    public MemberGame viewMemberGame(@PathVariable("memberId") int memberId, @PathVariable("id") Long id){
        //회원 확인하는 코드 넣기
        MemberGame memberGame = new MemberGame();
        return memberGame;//memberGameDto로 변경하기
    }

    //라이브러리에 게임추가
    @PostMapping
    @Transactional
    @Operation(summary = "라이브러리에 멤버게임 추가")
    public RsData addMemberGame(@PathVariable("memberId") int memberId, @RequestBody MemberGameAddRequest request){
        //Member actor = rq.getActor();
        Member actor = memberService.findByEmail("john@gmail.com").get();
        //check if memberId  == actor.getId()
        Game game = gameService.findById(request.gameId()).orElseThrow();
        //체크 더 넣기 (플랫폼도 똑같은 중복게임인지 체크 등)
        MemberGame memberGame = memberService.addToLibrary(request.platform(),request.playtime(),request.isFavorite(),actor, game);
        memberService.flush();
        return new RsData<>(
                "201-1",
                "라이브러리에 게임 %s가 추가되었습니다.".formatted(game.getName()),
                new MemberGameDto(memberGame)
        );
    }
    //라이브러리에서 게임삭제

    //게임정보 업데이트하기 (favorite, review, playtime 등)
}