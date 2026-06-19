package com.example.haksikmokjang.chat.chatreview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatReview is a Querydsl query type for ChatReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatReview extends EntityPathBase<ChatReview> {

    private static final long serialVersionUID = -672746263L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatReview chatReview = new QChatReview("chatReview");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final NumberPath<Long> chatReviewId = createNumber("chatReviewId", Long.class);

    public final com.example.haksikmokjang.chat.chatroom.domain.QChatRoom chatRoom;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> mannerScore = createNumber("mannerScore", Integer.class);

    public final StringPath noShowYn = createString("noShowYn");

    public final com.example.haksikmokjang.member.core.domain.QMember reviewer;

    public final com.example.haksikmokjang.member.core.domain.QMember targetMember;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatReview(String variable) {
        this(ChatReview.class, forVariable(variable), INITS);
    }

    public QChatReview(Path<? extends ChatReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatReview(PathMetadata metadata, PathInits inits) {
        this(ChatReview.class, metadata, inits);
    }

    public QChatReview(Class<? extends ChatReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new com.example.haksikmokjang.chat.chatroom.domain.QChatRoom(forProperty("chatRoom")) : null;
        this.reviewer = inits.isInitialized("reviewer") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("reviewer")) : null;
        this.targetMember = inits.isInitialized("targetMember") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("targetMember")) : null;
    }

}

