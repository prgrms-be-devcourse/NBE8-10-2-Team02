package com.back.domain.post.postComment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostComment is a Querydsl query type for PostComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostComment extends EntityPathBase<PostComment> {

    private static final long serialVersionUID = -1751484287L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostComment postComment = new QPostComment("postComment");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.member.member.entity.QMember author;

    public final ListPath<PostComment, QPostComment> children = this.<PostComment, QPostComment>createList("children", PostComment.class, QPostComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createDate = createDateTime("createDate", java.time.LocalDateTime.class);

    public final BooleanPath deleted = createBoolean("deleted");

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final DateTimePath<java.time.LocalDateTime> modifyDate = createDateTime("modifyDate", java.time.LocalDateTime.class);

    public final QPostComment parent;

    public final com.back.domain.post.post.entity.QPost post;

    public QPostComment(String variable) {
        this(PostComment.class, forVariable(variable), INITS);
    }

    public QPostComment(Path<? extends PostComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostComment(PathMetadata metadata, PathInits inits) {
        this(PostComment.class, metadata, inits);
    }

    public QPostComment(Class<? extends PostComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.back.domain.member.member.entity.QMember(forProperty("author")) : null;
        this.parent = inits.isInitialized("parent") ? new QPostComment(forProperty("parent"), inits.get("parent")) : null;
        this.post = inits.isInitialized("post") ? new com.back.domain.post.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
    }

}

