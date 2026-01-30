package com.back.domain.member.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 826426997L;

    public static final QMember member = new QMember("member1");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final StringPath apiKey = createString("apiKey");

    public final DateTimePath<java.time.LocalDateTime> createDate = createDateTime("createDate", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final ListPath<com.back.domain.member.memberGame.entity.MemberGame, com.back.domain.member.memberGame.entity.QMemberGame> library = this.<com.back.domain.member.memberGame.entity.MemberGame, com.back.domain.member.memberGame.entity.QMemberGame>createList("library", com.back.domain.member.memberGame.entity.MemberGame.class, com.back.domain.member.memberGame.entity.QMemberGame.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> modifyDate = createDateTime("modifyDate", java.time.LocalDateTime.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

