package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.back.domain.game.game.dto.GameSearchCondition;

import java.util.List;

import static com.back.domain.game.game.entity.QGame.game;
import static com.back.domain.game.game.entity.QGameGenre.gameGenre;
import static com.back.domain.game.game.entity.QGamePlatform.gamePlatform;

@Repository
@RequiredArgsConstructor
public class GameSearchRepositoryImpl implements GameSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Game> searchByCondition(GameSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 게임 이름 자연어 검색
        if (condition.getQuery() != null && !condition.getQuery().isBlank()) {
            builder.and(
                    game.name.containsIgnoreCase(condition.getQuery())
            );
        }

        // 2. 장르 필터
        if (condition.getGenreIds() != null && !condition.getGenreIds().isEmpty()) {
            builder.and(
                    gameGenre.genre.igdbId.in(condition.getGenreIds())
            );
        }

        // 3. 플랫폼 필터
        if (condition.getPlatformIgdbIds() != null) {
            builder.and(
                    gamePlatform.platform.igdbId.in(condition.getPlatformIgdbIds())
            );
        }

        return queryFactory
                .selectDistinct(game)
                .from(game)
                .leftJoin(game.gameGenres, gameGenre)
                .leftJoin(game.gamePlatforms, gamePlatform)
                .where(builder)
                .fetch();
    }
}
