package com.example.haksikmokjang.matching.matchingrequest.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatching is a Querydsl query type for Matching
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatching extends EntityPathBase<Matching> {

    private static final long serialVersionUID = -1728008509L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatching matching = new QMatching("matching");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final NumberPath<Long> chatRoomId = createNumber("chatRoomId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> matchingId = createNumber("matchingId", Long.class);

    public final EnumPath<com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType> matchingType = createEnum("matchingType", com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType.class);

    public final EnumPath<com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode> mode = createEnum("mode", com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode.class);

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final com.example.haksikmokjang.member.signup.user.domain.QUserProfile requester;

    public final DateTimePath<java.time.LocalDateTime> respondedAt = createDateTime("respondedAt", java.time.LocalDateTime.class);

    public final EnumPath<MatchingStatus> status = createEnum("status", MatchingStatus.class);

    public final com.example.haksikmokjang.member.signup.user.domain.QUserProfile target;

    public final com.example.haksikmokjang.matching.matchingwaiting.domain.QMatchingWaiting targetWaiting;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMatching(String variable) {
        this(Matching.class, forVariable(variable), INITS);
    }

    public QMatching(Path<? extends Matching> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatching(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatching(PathMetadata metadata, PathInits inits) {
        this(Matching.class, metadata, inits);
    }

    public QMatching(Class<? extends Matching> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.requester = inits.isInitialized("requester") ? new com.example.haksikmokjang.member.signup.user.domain.QUserProfile(forProperty("requester"), inits.get("requester")) : null;
        this.target = inits.isInitialized("target") ? new com.example.haksikmokjang.member.signup.user.domain.QUserProfile(forProperty("target"), inits.get("target")) : null;
        this.targetWaiting = inits.isInitialized("targetWaiting") ? new com.example.haksikmokjang.matching.matchingwaiting.domain.QMatchingWaiting(forProperty("targetWaiting"), inits.get("targetWaiting")) : null;
    }

}

