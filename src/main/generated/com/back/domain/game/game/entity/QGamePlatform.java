package com.back.domain.game.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGamePlatform is a Querydsl query type for GamePlatform
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGamePlatform extends EntityPathBase<GamePlatform> {

    private static final long serialVersionUID = 1553241632L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGamePlatform gamePlatform = new QGamePlatform("gamePlatform");

    public final QGame game;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPlatform platform;

    public QGamePlatform(String variable) {
        this(GamePlatform.class, forVariable(variable), INITS);
    }

    public QGamePlatform(Path<? extends GamePlatform> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGamePlatform(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGamePlatform(PathMetadata metadata, PathInits inits) {
        this(GamePlatform.class, metadata, inits);
    }

    public QGamePlatform(Class<? extends GamePlatform> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.game = inits.isInitialized("game") ? new QGame(forProperty("game")) : null;
        this.platform = inits.isInitialized("platform") ? new QPlatform(forProperty("platform")) : null;
    }

}

