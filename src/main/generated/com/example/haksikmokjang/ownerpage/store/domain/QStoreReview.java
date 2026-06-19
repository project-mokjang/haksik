package com.example.haksikmokjang.ownerpage.store.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreReview is a Querydsl query type for StoreReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreReview extends EntityPathBase<StoreReview> {

    private static final long serialVersionUID = 1914502033L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreReview storeReview = new QStoreReview("storeReview");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final QReservation reservation;

    public final NumberPath<Long> reviewId = createNumber("reviewId", Long.class);

    public final EnumPath<ReviewStatus> status = createEnum("status", ReviewStatus.class);

    public final QStore store;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreReview(String variable) {
        this(StoreReview.class, forVariable(variable), INITS);
    }

    public QStoreReview(Path<? extends StoreReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreReview(PathMetadata metadata, PathInits inits) {
        this(StoreReview.class, metadata, inits);
    }

    public QStoreReview(Class<? extends StoreReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
        this.reservation = inits.isInitialized("reservation") ? new QReservation(forProperty("reservation"), inits.get("reservation")) : null;
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

