package com.example.haksikmokjang.chat.chatmessage.dto;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatMessageEditHistory is a Querydsl query type for ChatMessageEditHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMessageEditHistory extends EntityPathBase<ChatMessageEditHistory> {

    private static final long serialVersionUID = -1294040874L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatMessageEditHistory chatMessageEditHistory = new QChatMessageEditHistory("chatMessageEditHistory");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final StringPath afterMessage = createString("afterMessage");

    public final StringPath beforeMessage = createString("beforeMessage");

    public final com.example.haksikmokjang.chat.chatmessage.domain.QChatMessage chatMessage;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.haksikmokjang.member.core.domain.QMember editedBy;

    public final NumberPath<Long> editHistoryId = createNumber("editHistoryId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatMessageEditHistory(String variable) {
        this(ChatMessageEditHistory.class, forVariable(variable), INITS);
    }

    public QChatMessageEditHistory(Path<? extends ChatMessageEditHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatMessageEditHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatMessageEditHistory(PathMetadata metadata, PathInits inits) {
        this(ChatMessageEditHistory.class, metadata, inits);
    }

    public QChatMessageEditHistory(Class<? extends ChatMessageEditHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatMessage = inits.isInitialized("chatMessage") ? new com.example.haksikmokjang.chat.chatmessage.domain.QChatMessage(forProperty("chatMessage"), inits.get("chatMessage")) : null;
        this.editedBy = inits.isInitialized("editedBy") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("editedBy")) : null;
    }

}

