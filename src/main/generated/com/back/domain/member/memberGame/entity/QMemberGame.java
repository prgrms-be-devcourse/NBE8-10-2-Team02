package com.back.domain.member.memberGame.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberGame is a Querydsl query type for MemberGame
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberGame extends EntityPathBase<MemberGame> {

    private static final long serialVersionUID = -1454418791L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberGame memberGame = new QMemberGame("memberGame");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.game.game.entity.QGame game;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final BooleanPath isFavorite = createBoolean("isFavorite");

    public final com.back.domain.member.member.entity.QMember member;

    public final StringPath platform = createString("platform");

    public final NumberPath<Double> playtime = createNumber("playtime", Double.class);

    public QMemberGame(String variable) {
        this(MemberGame.class, forVariable(variable), INITS);
    }

    public QMemberGame(Path<? extends MemberGame> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberGame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberGame(PathMetadata metadata, PathInits inits) {
        this(MemberGame.class, metadata, inits);
    }

    public QMemberGame(Class<? extends MemberGame> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.game = inits.isInitialized("game") ? new com.back.domain.game.game.entity.QGame(forProperty("game")) : null;
        this.member = inits.isInitialized("member") ? new com.back.domain.member.member.entity.QMember(forProperty("member")) : null;
    }

}

