package com.example.haksikmokjang.matching.matchingwaiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchingWaiting is a Querydsl query type for MatchingWaiting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchingWaiting extends EntityPathBase<MatchingWaiting> {

    private static final long serialVersionUID = 1584198796L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchingWaiting matchingWaiting = new QMatchingWaiting("matchingWaiting");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> currentParticipants = createNumber("currentParticipants", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final EnumPath<MatchingType> matchingType = createEnum("matchingType", MatchingType.class);

    public final NumberPath<Integer> maxParticipants = createNumber("maxParticipants", Integer.class);

    public final StringPath message = createString("message");

    public final EnumPath<MatchingMode> mode = createEnum("mode", MatchingMode.class);

    public final EnumPath<MatchingWaitingStatus> status = createEnum("status", MatchingWaitingStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.haksikmokjang.member.signup.user.domain.QUserProfile userProfile;

    public final NumberPath<Long> waitingId = createNumber("waitingId", Long.class);

    public QMatchingWaiting(String variable) {
        this(MatchingWaiting.class, forVariable(variable), INITS);
    }

    public QMatchingWaiting(Path<? extends MatchingWaiting> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchingWaiting(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchingWaiting(PathMetadata metadata, PathInits inits) {
        this(MatchingWaiting.class, metadata, inits);
    }

    public QMatchingWaiting(Class<? extends MatchingWaiting> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userProfile = inits.isInitialized("userProfile") ? new com.example.haksikmokjang.member.signup.user.domain.QUserProfile(forProperty("userProfile"), inits.get("userProfile")) : null;
    }

}

