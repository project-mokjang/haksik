package com.example.haksikmokjang.chat.chatform.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatFormAnswer is a Querydsl query type for ChatFormAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatFormAnswer extends EntityPathBase<ChatFormAnswer> {

    private static final long serialVersionUID = 1372300383L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatFormAnswer chatFormAnswer = new QChatFormAnswer("chatFormAnswer");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final QChatForm chatForm;

    public final NumberPath<Long> chatFormAnswerId = createNumber("chatFormAnswerId", Long.class);

    public final QChatFormOption chatFormOption;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.haksikmokjang.member.core.domain.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatFormAnswer(String variable) {
        this(ChatFormAnswer.class, forVariable(variable), INITS);
    }

    public QChatFormAnswer(Path<? extends ChatFormAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatFormAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatFormAnswer(PathMetadata metadata, PathInits inits) {
        this(ChatFormAnswer.class, metadata, inits);
    }

    public QChatFormAnswer(Class<? extends ChatFormAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatForm = inits.isInitialized("chatForm") ? new QChatForm(forProperty("chatForm"), inits.get("chatForm")) : null;
        this.chatFormOption = inits.isInitialized("chatFormOption") ? new QChatFormOption(forProperty("chatFormOption"), inits.get("chatFormOption")) : null;
        this.member = inits.isInitialized("member") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("member")) : null;
    }

}

