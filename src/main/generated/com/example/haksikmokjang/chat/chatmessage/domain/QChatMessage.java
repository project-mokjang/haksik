package com.example.haksikmokjang.chat.chatmessage.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatMessage is a Querydsl query type for ChatMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMessage extends EntityPathBase<ChatMessage> {

    private static final long serialVersionUID = 1250902257L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatMessage chatMessage = new QChatMessage("chatMessage");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final NumberPath<Long> chatMessageId = createNumber("chatMessageId", Long.class);

    public final com.example.haksikmokjang.chat.chatroom.domain.QChatRoom chatRoom;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath deleted = createBoolean("deleted");

    public final BooleanPath edited = createBoolean("edited");

    public final DateTimePath<java.time.LocalDateTime> editedAt = createDateTime("editedAt", java.time.LocalDateTime.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath message = createString("message");

    public final EnumPath<ChatMessageType> messageType = createEnum("messageType", ChatMessageType.class);

    public final com.example.haksikmokjang.member.core.domain.QMember sender;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatMessage(String variable) {
        this(ChatMessage.class, forVariable(variable), INITS);
    }

    public QChatMessage(Path<? extends ChatMessage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatMessage(PathMetadata metadata, PathInits inits) {
        this(ChatMessage.class, metadata, inits);
    }

    public QChatMessage(Class<? extends ChatMessage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new com.example.haksikmokjang.chat.chatroom.domain.QChatRoom(forProperty("chatRoom")) : null;
        this.sender = inits.isInitialized("sender") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("sender")) : null;
    }

}

