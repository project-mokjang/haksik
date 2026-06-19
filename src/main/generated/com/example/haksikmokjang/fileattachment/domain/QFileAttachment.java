package com.example.haksikmokjang.fileattachment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFileAttachment is a Querydsl query type for FileAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileAttachment extends EntityPathBase<FileAttachment> {

    private static final long serialVersionUID = 839335523L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFileAttachment fileAttachment = new QFileAttachment("fileAttachment");

    public final com.example.haksikmokjang.global.entity.QCreatedTimeEntity _super = new com.example.haksikmokjang.global.entity.QCreatedTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath extension = createString("extension");

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final StringPath originalName = createString("originalName");

    public final StringPath storedPath = createString("storedPath");

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final StringPath targetType = createString("targetType");

    public final com.example.haksikmokjang.member.core.domain.QMember uploader;

    public QFileAttachment(String variable) {
        this(FileAttachment.class, forVariable(variable), INITS);
    }

    public QFileAttachment(Path<? extends FileAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFileAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFileAttachment(PathMetadata metadata, PathInits inits) {
        this(FileAttachment.class, metadata, inits);
    }

    public QFileAttachment(Class<? extends FileAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.uploader = inits.isInitialized("uploader") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("uploader")) : null;
    }

}

