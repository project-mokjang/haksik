package com.example.haksikmokjang.member.terms.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTerms is a Querydsl query type for Terms
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTerms extends EntityPathBase<Terms> {

    private static final long serialVersionUID = -1705544945L;

    public static final QTerms terms = new QTerms("terms");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> effectiveAt = createDateTime("effectiveAt", java.time.LocalDateTime.class);

    public final StringPath requiredYn = createString("requiredYn");

    public final NumberPath<Long> termsId = createNumber("termsId", Long.class);

    public final StringPath title = createString("title");

    public final StringPath version = createString("version");

    public QTerms(String variable) {
        super(Terms.class, forVariable(variable));
    }

    public QTerms(Path<? extends Terms> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTerms(PathMetadata metadata) {
        super(Terms.class, metadata);
    }

}

