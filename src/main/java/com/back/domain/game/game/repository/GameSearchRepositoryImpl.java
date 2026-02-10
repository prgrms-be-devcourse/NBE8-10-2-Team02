package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
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
//    추후 DB검색 확장용

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Game> searchByCondition(GameSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 게임 이름 자연어 검색

        if (condition.getQuery() != null && !condition.getQuery().isBlank()) {

            StringExpression normalizedName =
                    Expressions.stringTemplate(
                            "replace({0}, ' ', '')",
                            game.name
                    );

            builder.and(
                    normalizedName.containsIgnoreCase(condition.getQuery())
            );
        }

        // 2. 장르 필터
        if (condition.getGenreIds() != null && !condition.getGenreIds().isEmpty()) {
            builder.and(
                    gameGenre.genre.igdbId.in(condition.getGenreIds())
            );
        }
        boolean hasPlatform =
                condition.getPlatformIgdbIds() != null
                        && !condition.getPlatformIgdbIds().isEmpty();

        var query = queryFactory
                .selectDistinct(game)
                .from(game)
                .leftJoin(gameGenre).on(gameGenre.game.eq(game));

        if (hasPlatform) {
            // 플랫폼 필터 있을 때
            query.join(gamePlatform).on(gamePlatform.game.eq(game));
            builder.and(
                    gamePlatform.platform.igdbId.in(condition.getPlatformIgdbIds())
            );
        } else {
            // 플랫폼 필터 없을 때
            query.leftJoin(gamePlatform).on(gamePlatform.game.eq(game));
        }



        return query
                .where(builder)
                .fetch();
    }
}
