package com.example.haksikmokjang.chat.chatform.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatFormOption is a Querydsl query type for ChatFormOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatFormOption extends EntityPathBase<ChatFormOption> {

    private static final long serialVersionUID = 1774972182L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatFormOption chatFormOption = new QChatFormOption("chatFormOption");

    public final com.example.haksikmokjang.global.entity.QBaseEntity _super = new com.example.haksikmokjang.global.entity.QBaseEntity(this);

    public final StringPath address = createString("address");

    public final QChatForm chatForm;

    public final NumberPath<Long> chatFormOptionId = createNumber("chatFormOptionId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.example.haksikmokjang.member.core.domain.QMember createdByMember;

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath mapUrl = createString("mapUrl");

    public final NumberPath<Integer> optionOrder = createNumber("optionOrder", Integer.class);

    public final StringPath optionText = createString("optionText");

    public final StringPath placeName = createString("placeName");

    public final EnumPath<ChatPlaceSource> placeSource = createEnum("placeSource", ChatPlaceSource.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatFormOption(String variable) {
        this(ChatFormOption.class, forVariable(variable), INITS);
    }

    public QChatFormOption(Path<? extends ChatFormOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatFormOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatFormOption(PathMetadata metadata, PathInits inits) {
        this(ChatFormOption.class, metadata, inits);
    }

    public QChatFormOption(Class<? extends ChatFormOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatForm = inits.isInitialized("chatForm") ? new QChatForm(forProperty("chatForm"), inits.get("chatForm")) : null;
        this.createdByMember = inits.isInitialized("createdByMember") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("createdByMember")) : null;
    }

}

