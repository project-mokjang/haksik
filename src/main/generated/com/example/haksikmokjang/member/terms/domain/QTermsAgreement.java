package com.example.haksikmokjang.member.terms.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTermsAgreement is a Querydsl query type for TermsAgreement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTermsAgreement extends EntityPathBase<TermsAgreement> {

    private static final long serialVersionUID = 390266811L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTermsAgreement termsAgreement = new QTermsAgreement("termsAgreement");

    public final DateTimePath<java.time.LocalDateTime> agreedAt = createDateTime("agreedAt", java.time.LocalDateTime.class);

    public final StringPath agreedYn = createString("agreedYn");

    public final NumberPath<Long> agreementId = createNumber("agreementId", Long.class);

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    public final QTerms terms;

    public QTermsAgreement(String variable) {
        this(TermsAgreement.class, forVariable(variable), INITS);
    }

    public QTermsAgreement(Path<? extends TermsAgreement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTermsAgreement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTermsAgreement(PathMetadata metadata, PathInits inits) {
        this(TermsAgreement.class, metadata, inits);
    }

    public QTermsAgreement(Class<? extends TermsAgreement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
        this.terms = inits.isInitialized("terms") ? new QTerms(forProperty("terms")) : null;
    }

}

