package com.example.haksikmokjang.community.post.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.school.domain.School;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder

public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 🚨 팩트: DB 성능을 위해 게시글 자체에 학교 ID를 박아둡니다. 전국구 글이면 null이 들어갑니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    // 🚨 팩트: 전국구(GLOBAL)인지 우리학교(SCHOOL)인지 직관적으로 쪼갭니다.
    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 20)
    private BoardType boardType;

    // 🚨 팩트: 요구하신 세부 말머리 (FREE, FOOD, QUESTION, MEETUP)
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private PostCategory category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "anonymous_yn", nullable = false, length = 1)
    private String anonymousYn;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    // 게시글 진입 시 조회수를 1 올리기
    public void addViewCount() {
        this.viewCount++;
    }

    //게시글 수정/삭제

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void deletePost() {
        // DB에서 날리지 않고 상태만 DELETE로 바꿔 화면에 안 보이게
        this.status = PostStatus.DELETE;
    }

    // 게시글 숨김 처리
    public void hide() {
        this.status = PostStatus.HIDDEN;
    }
}