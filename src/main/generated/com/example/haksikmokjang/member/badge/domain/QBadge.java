package com.example.haksikmokjang.member.badge.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBadge is a Querydsl query type for Badge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBadge extends EntityPathBase<Badge> {

    private static final long serialVersionUID = -1188118385L;

    public static final QBadge badge = new QBadge("badge");

    public final NumberPath<Long> badgeId = createNumber("badgeId", Long.class);

    public final StringPath badgeName = createString("badgeName");

    public final StringPath conditionText = createString("conditionText");

    public final EnumPath<BadgeConditionType> conditionType = createEnum("conditionType", BadgeConditionType.class);

    public final NumberPath<Integer> conditionValue = createNumber("conditionValue", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath emoji = createString("emoji");

    public QBadge(String variable) {
        super(Badge.class, forVariable(variable));
    }

    public QBadge(Path<? extends Badge> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBadge(PathMetadata metadata) {
        super(Badge.class, metadata);
    }

}

