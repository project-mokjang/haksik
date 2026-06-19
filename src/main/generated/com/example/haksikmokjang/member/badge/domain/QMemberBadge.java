package com.example.haksikmokjang.member.badge.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberBadge is a Querydsl query type for MemberBadge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberBadge extends EntityPathBase<MemberBadge> {

    private static final long serialVersionUID = 67990805L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberBadge memberBadge = new QMemberBadge("memberBadge");

    public final QBadge badge;

    public final DateTimePath<java.time.LocalDateTime> earnedAt = createDateTime("earnedAt", java.time.LocalDateTime.class);

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    public final NumberPath<Long> memberBadgeId = createNumber("memberBadgeId", Long.class);

    public final NumberPath<Integer> representativeOrder = createNumber("representativeOrder", Integer.class);

    public QMemberBadge(String variable) {
        this(MemberBadge.class, forVariable(variable), INITS);
    }

    public QMemberBadge(Path<? extends MemberBadge> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberBadge(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberBadge(PathMetadata metadata, PathInits inits) {
        this(MemberBadge.class, metadata, inits);
    }

    public QMemberBadge(Class<? extends MemberBadge> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.badge = inits.isInitialized("badge") ? new QBadge(forProperty("badge")) : null;
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
    }

}

