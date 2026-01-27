package com.back.domain.member.memberGame.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.memberGame.StatusEnum;
import com.back.domain.member.memberGame.dto.MemberGameUpdateRequest;
import com.back.domain.member.memberGame.entity.MemberGame;
import com.back.domain.member.memberGame.repository.MemberGameRepository;
import com.back.domain.review.entity.Review;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberGameService {
    private final MemberRepository memberRepository;
    private final MemberGameRepository memberGameRepository;

    public MemberGame addToLibrary(String platform, double playtime, boolean isFavorite, StatusEnum status, Member member, Game game) {
        // Check for duplicate game in library
        Optional<MemberGame> existingGame = memberGameRepository.findByMemberIdAndGameId(member.getId(), game.getId());
        if (existingGame.isPresent()) {
            throw new ServiceException("400-2", "이미 라이브러리에 존재하는 게임입니다.");
        }
        return member.addMemberGame(platform, playtime, isFavorite, status, game);
    }

    public boolean removeFromLibrary(Member member, int id) {
        return member.removeGame(id);
    }

    public MemberGame findByMemberAndGame(int memberId, int gameId) {
        return memberGameRepository.findByMemberIdAndGameId(memberId, gameId)
                .orElseThrow(() -> new ServiceException("404", "MemberGame not found"));
    }
    @Transactional
    public MemberGame updateMemberGame(int memberGameId, int memberId, @Valid MemberGameUpdateRequest request){
        MemberGame memberGame = memberGameRepository.findById(memberGameId)
                .orElseThrow(() -> new ServiceException("404", "Game not found"));
        // Verify ownership
        if (memberGame.getMember().getId() != memberId) {
            throw new ServiceException("403", "Not your game");
        }
        // Update fields
        if (request.status() != null) memberGame.setStatus(request.status());
        if (request.playtime() != null) memberGame.setPlaytime(request.playtime());
        if (request.isFavorite() != null) memberGame.setFavorite(request.isFavorite());
        if (request.platform() != null) memberGame.setPlatform(request.platform());
        return memberGame;
    }

    @Transactional
    public MemberGame updateReview(int memberId, int gameId, Review review){
        MemberGame memberGame = memberGameRepository.findByMemberIdAndGameId(memberId, gameId)
                .orElseThrow(() -> new ServiceException("404", "MemberGame not found"));
        // Verify ownership
        if (memberGame.getMember().getId() != memberId) {
            throw new ServiceException("403", "Not your game");
        }
        // Update fields
        memberGame.setReview(review);
        return memberGame;
    }


}