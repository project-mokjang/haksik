package com.example.haksikmokjang.member.core.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberLocation is a Querydsl query type for MemberLocation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberLocation extends EntityPathBase<MemberLocation> {

    private static final long serialVersionUID = -690449099L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberLocation memberLocation = new QMemberLocation("memberLocation");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> accuracyRangeM = createNumber("accuracyRangeM", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final StringPath locationPublicYn = createString("locationPublicYn");

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final NumberPath<Long> memberLocationId = createNumber("memberLocationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.haksikmokjang.member.signup.user.domain.QUserProfile userProfile;

    public QMemberLocation(String variable) {
        this(MemberLocation.class, forVariable(variable), INITS);
    }

    public QMemberLocation(Path<? extends MemberLocation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberLocation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberLocation(PathMetadata metadata, PathInits inits) {
        this(MemberLocation.class, metadata, inits);
    }

    public QMemberLocation(Class<? extends MemberLocation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userProfile = inits.isInitialized("userProfile") ? new com.example.haksikmokjang.member.signup.user.domain.QUserProfile(forProperty("userProfile"), inits.get("userProfile")) : null;
    }

}

