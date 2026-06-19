package com.example.haksikmokjang.member.emailverification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailVerification is a Querydsl query type for EmailVerification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailVerification extends EntityPathBase<EmailVerification> {

    private static final long serialVersionUID = 1261877423L;

    public static final QEmailVerification emailVerification = new QEmailVerification("emailVerification");

    public final com.example.haksikmokjang.global.entity.QCreatedTimeEntity _super = new com.example.haksikmokjang.global.entity.QCreatedTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final EnumPath<EmailPurpose> purpose = createEnum("purpose", EmailPurpose.class);

    public final StringPath verificationCode = createString("verificationCode");

    public final NumberPath<Long> verificationId = createNumber("verificationId", Long.class);

    public final StringPath verifiedYn = createString("verifiedYn");

    public QEmailVerification(String variable) {
        super(EmailVerification.class, forVariable(variable));
    }

    public QEmailVerification(Path<? extends EmailVerification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailVerification(PathMetadata metadata) {
        super(EmailVerification.class, metadata);
    }

}

