package com.example.haksikmokjang.chat.chatform.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatForm is a Querydsl query type for ChatForm
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatForm extends EntityPathBase<ChatForm> {

    private static final long serialVersionUID = 1199385921L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatForm chatForm = new QChatForm("chatForm");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final NumberPath<Long> chatFormId = createNumber("chatFormId", Long.class);

    public final com.example.haksikmokjang.chat.chatroom.domain.QChatRoom chatRoom;

    public final StringPath closedYn = createString("closedYn");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.haksikmokjang.member.core.domain.QMember creator;

    public final EnumPath<ChatFormType> formType = createEnum("formType", ChatFormType.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatForm(String variable) {
        this(ChatForm.class, forVariable(variable), INITS);
    }

    public QChatForm(Path<? extends ChatForm> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatForm(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatForm(PathMetadata metadata, PathInits inits) {
        this(ChatForm.class, metadata, inits);
    }

    public QChatForm(Class<? extends ChatForm> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new com.example.haksikmokjang.chat.chatroom.domain.QChatRoom(forProperty("chatRoom")) : null;
        this.creator = inits.isInitialized("creator") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("creator")) : null;
    }

}

