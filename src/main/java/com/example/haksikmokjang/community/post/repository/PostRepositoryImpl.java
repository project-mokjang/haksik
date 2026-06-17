package com.example.haksikmokjang.community.post.repository;


import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.domain.PostStatus;
import com.example.haksikmokjang.community.post.dto.PostListResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.haksikmokjang.community.post.domain.QPost.post;
import static com.example.haksikmokjang.member.core.domain.QMember.member;
import static com.example.haksikmokjang.member.signup.user.domain.QUserProfile.userProfile;


// Q클래스 설계도 import

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostListResponse> searchPosts(
            BoardType boardType,
            Long schoolId,
            PostCategory category,
            String keyword,
            Long lastPostId,
            int pageSize,
            String loginId,
            String sort) {

        OrderSpecifier<?> orderSpecifier =
                "POPULAR".equals(sort) ? post.viewCount.desc() : post.createdAt.desc();
        return queryFactory
                .select(Projections.constructor(PostListResponse.class,
                        post.postId,
                        post.category, //stringValue()를 지우고 postlistResponse -> Enum 뼈대 다이렉트로 받음.
                        post.title,
                        new CaseBuilder()
                                .when(post.anonymousYn.eq("Y")).then("익명")
                                .otherwise(userProfile.nickname),
                        post.viewCount,
                        post.createdAt,
                        new CaseBuilder()
                                .when(post.member.loginId.eq(loginId)).then(true)
                                .otherwise(false)
                ))
                .from(post)
                .join(post.member, member)// 작성자 찾고
                .join(userProfile).on(userProfile.member.eq(member)) // 작성자와 연결된 userProfile 찾기.
                .where(
                        post.status.eq(PostStatus.ACTIVE), // 삭제 안 된 정상 글만
                        ltPostId(lastPostId),              // 무한 스크롤
                        eqBoardType(boardType),            // 전국구 or 우리학교 필터
                        eqSchoolId(schoolId),              // 우리학교일 경우 학교 ID 필터
                        eqCategory(category),              // 말머리(자유, 맛집 등) 필터
                        containsKeyword(keyword)           // 제목/내용 검색 필터
                )
                .orderBy(orderSpecifier, post.postId.desc())
                .limit(pageSize)             // 딱 요청한 개수(예: 10개)만 가져옴
                .fetch();
    }
    // 아래는 값이 널(null)이면 조건에서 빼버림

    private BooleanExpression ltPostId(Long lastPostId) {
        // lastPostId가 없으면 첫 페이지이므로 조건 무시 있으면 그 번호보다 옛날 글 조회
        return lastPostId != null ? post.postId.lt(lastPostId) : null;
    }

    private BooleanExpression eqBoardType(BoardType boardType) {
        return boardType != null ? post.boardType.eq(boardType) : null;
    }

    private BooleanExpression eqSchoolId(Long schoolId) {
        return schoolId != null ? post.school.schoolId.eq(schoolId) : null;
    }

    private BooleanExpression eqCategory(PostCategory category) {
        return category != null ? post.category.eq(category) : null;
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건 무시
        }
        // 검색어가 있으면 제목이나 내용에 포함되어 있는지 검사
        return post.title.contains(keyword).or(post.content.contains(keyword));
    }

}
