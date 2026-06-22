package com.example.haksikmokjang.member.signup.owner.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOwnerProfile is a Querydsl query type for OwnerProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOwnerProfile extends EntityPathBase<OwnerProfile> {

    private static final long serialVersionUID = 1506439802L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOwnerProfile ownerProfile = new QOwnerProfile("ownerProfile");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final EnumPath<ApprovalStatus> approvalStatus = createEnum("approvalStatus", ApprovalStatus.class);

    public final StringPath businessName = createString("businessName");

    public final StringPath businessNumber = createString("businessNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    public final StringPath ownerName = createString("ownerName");

    public final StringPath ownerPhone = createString("ownerPhone");

    public final NumberPath<Long> ownerProfileId = createNumber("ownerProfileId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final com.example.haksikmokjang.member.core.domain.QMember processedBy;

    public final StringPath rejectedReason = createString("rejectedReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOwnerProfile(String variable) {
        this(OwnerProfile.class, forVariable(variable), INITS);
    }

    public QOwnerProfile(Path<? extends OwnerProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOwnerProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOwnerProfile(PathMetadata metadata, PathInits inits) {
        this(OwnerProfile.class, metadata, inits);
    }

    public QOwnerProfile(Class<? extends OwnerProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
        this.processedBy = inits.isInitialized("processedBy") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("processedBy")) : null;
    }

}

