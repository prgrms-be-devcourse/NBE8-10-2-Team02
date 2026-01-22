package com.back.domain.game.game.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlatform is a Querydsl query type for Platform
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlatform extends EntityPathBase<Platform> {

    private static final long serialVersionUID = 875167118L;

    public static final QPlatform platform = new QPlatform("platform");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> igdbId = createNumber("igdbId", Long.class);

    public final StringPath name = createString("name");

    public QPlatform(String variable) {
        super(Platform.class, forVariable(variable));
    }

    public QPlatform(Path<? extends Platform> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlatform(PathMetadata metadata) {
        super(Platform.class, metadata);
    }

}

