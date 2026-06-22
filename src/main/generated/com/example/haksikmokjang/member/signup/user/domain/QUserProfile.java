package com.example.haksikmokjang.member.signup.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserProfile is a Querydsl query type for UserProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProfile extends EntityPathBase<UserProfile> {

    private static final long serialVersionUID = 1769979660L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserProfile userProfile = new QUserProfile("userProfile");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath department = createString("department");

    public final EnumPath<com.example.haksikmokjang.member.core.domain.Gender> gender = createEnum("gender", com.example.haksikmokjang.member.core.domain.Gender.class);

    public final NumberPath<java.math.BigDecimal> mannerTemperature = createNumber("mannerTemperature", java.math.BigDecimal.class);

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final NumberPath<Integer> noShowCount = createNumber("noShowCount", Integer.class);

    public final StringPath preferredFoodCategory = createString("preferredFoodCategory");

    public final com.example.haksikmokjang.fileattachment.domain.QFileAttachment profileImage;

    public final com.example.haksikmokjang.school.domain.QSchool school;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userProfileId = createNumber("userProfileId", Long.class);

    public QUserProfile(String variable) {
        this(UserProfile.class, forVariable(variable), INITS);
    }

    public QUserProfile(Path<? extends UserProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserProfile(PathMetadata metadata, PathInits inits) {
        this(UserProfile.class, metadata, inits);
    }

    public QUserProfile(Class<? extends UserProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
        this.profileImage = inits.isInitialized("profileImage") ? new com.example.haksikmokjang.fileattachment.domain.QFileAttachment(forProperty("profileImage"), inits.get("profileImage")) : null;
        this.school = inits.isInitialized("school") ? new com.example.haksikmokjang.school.domain.QSchool(forProperty("school")) : null;
    }

}

