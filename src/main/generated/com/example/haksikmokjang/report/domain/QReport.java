package com.example.haksikmokjang.report.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = -1610221139L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReport report = new QReport("report");

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final com.example.haksikmokjang.member.core.domain.QMember processedBy;

    public final StringPath reason = createString("reason");

    public final com.example.haksikmokjang.member.core.domain.QMember reporter;

    public final NumberPath<Long> reportId = createNumber("reportId", Long.class);

    public final EnumPath<ReportStatus> status = createEnum("status", ReportStatus.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final StringPath targetType = createString("targetType");

    public QReport(String variable) {
        this(Report.class, forVariable(variable), INITS);
    }

    public QReport(Path<? extends Report> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReport(PathMetadata metadata, PathInits inits) {
        this(Report.class, metadata, inits);
    }

    public QReport(Class<? extends Report> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.processedBy = inits.isInitialized("processedBy") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("processedBy")) : null;
        this.reporter = inits.isInitialized("reporter") ? new com.example.haksikmokjang.member.core.domain.QMember(forProperty("reporter")) : null;
    }

}

