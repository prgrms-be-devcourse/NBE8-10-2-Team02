package com.back.domain.tag.postTag.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostTag is a Querydsl query type for PostTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostTag extends EntityPathBase<PostTag> {

    private static final long serialVersionUID = -757635257L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostTag postTag = new QPostTag("postTag");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final com.back.domain.post.post.entity.QPost post;

    public final com.back.domain.tag.tag.entity.QTag tag;

    public QPostTag(String variable) {
        this(PostTag.class, forVariable(variable), INITS);
    }

    public QPostTag(Path<? extends PostTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostTag(PathMetadata metadata, PathInits inits) {
        this(PostTag.class, metadata, inits);
    }

    public QPostTag(Class<? extends PostTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new com.back.domain.post.post.entity.QPost(forProperty("post")) : null;
        this.tag = inits.isInitialized("tag") ? new com.back.domain.tag.tag.entity.QTag(forProperty("tag")) : null;
    }

}

