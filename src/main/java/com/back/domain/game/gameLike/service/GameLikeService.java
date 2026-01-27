package com.back.domain.game.gameLike.service;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.repository.GameRepository;
import com.back.domain.game.gameLike.entity.GameLike;
import com.back.domain.game.gameLike.repository.GameLikeRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameLikeService {
    private final GameLikeRepository gameLikeRepository;
    private final GameRepository gameRepository;

    @Transactional
    public boolean toggleLike(Member member, long igdbId) {
        Game game = gameRepository.findByIgdbId(igdbId)
                .orElseThrow(() -> new ServiceException("404-1", "게임을 찾을 수 없습니다. igdbId" + igdbId));

        Optional<GameLike> existingLike = gameLikeRepository.findByMemberAndGame(member, game);

        if (existingLike.isPresent()) {
            // 이미 있으면 취소
            gameLikeRepository.delete(existingLike.get());
            game.decrementLikeCount();
            return false;
        } else {
            // 없으면 추가
            GameLike gameLike = GameLike.createGameLike(member, game);
            gameLikeRepository.save(gameLike);
            game.decrementLikeCount();
            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Member member, long igdbId) {
        if (member == null) return false;
        return gameLikeRepository.existsByMemberIdAndGameIgdbId(member.getId(), igdbId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(long igdbId) {
        return gameRepository.findByIgdbId(igdbId)
                .map(Game::getLikeCount)
                .orElse(0L);
    }

//    @Transactional(readOnly = true)
//    public List<Game> getLinkedGames(Member member) {
//        return gameLikeRepository.findLikedGamesByMember(member);
//    }
}
