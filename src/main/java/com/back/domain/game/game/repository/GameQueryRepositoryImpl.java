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
public class GameQueryRepositoryImpl  implements GameQueryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Game> searchByCondition(GameSearchCondition condition){

        BooleanBuilder builder = new BooleanBuilder();

        if(condition.getGenreIds() != null && !condition.getGenreIds().isEmpty()){
            builder.and(gameGenre.genre.id.in(condition.getGenreIds()));
        }

        if(condition.getPlatformIds() != null && !condition.getPlatformIds().isEmpty()) {
            builder.and(gamePlatform.platform.id.in(condition.getPlatformIds()));
        }

        return queryFactory
                .selectDistinct(game)
                .from(game)
                .leftJoin(gameGenre).on(gameGenre.game.eq(game))
                .leftJoin(gamePlatform).on(gamePlatform.game.eq(game))
                .where(builder)
                .fetch();
    }
}
