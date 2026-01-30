package com.back.domain.game.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = -1068390771L;

    public static final QGame game = new QGame("game");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final StringPath coverImageId = createString("coverImageId");

    public final ListPath<String, StringPath> developers = this.<String, StringPath>createList("developers", String.class, StringPath.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> firstReleaseDate = createDate("firstReleaseDate", java.time.LocalDate.class);

    public final ListPath<GameGenre, QGameGenre> gameGenres = this.<GameGenre, QGameGenre>createList("gameGenres", GameGenre.class, QGameGenre.class, PathInits.DIRECT2);

    public final ListPath<GamePlatform, QGamePlatform> gamePlatforms = this.<GamePlatform, QGamePlatform>createList("gamePlatforms", GamePlatform.class, QGamePlatform.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final NumberPath<Long> igdbId = createNumber("igdbId", Long.class);

    public final DateTimePath<java.time.Instant> lastFetchedAt = createDateTime("lastFetchedAt", java.time.Instant.class);

    public final StringPath name = createString("name");

    public final ListPath<String, StringPath> publishers = this.<String, StringPath>createList("publishers", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath summary = createString("summary");

    public QGame(String variable) {
        super(Game.class, forVariable(variable));
    }

    public QGame(Path<? extends Game> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGame(PathMetadata metadata) {
        super(Game.class, metadata);
    }

}

