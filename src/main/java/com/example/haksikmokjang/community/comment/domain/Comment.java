package com.example.haksikmokjang.community.comment.domain;

import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "COMMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //대댓글을 위한 Self-Join
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    //TEXT 타입 지정
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    //익명 여부 필드
    @Column(name = "anonymous_yn", nullable = false, length = 1)
    private String anonymousYn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status;

    @Builder
    public Comment(Post post, Member member, Comment parentComment, String content, String anonymousYn) {
        this.post = post;
        this.member = member;
        this.parentComment = parentComment; // 최상위 댓글이면 null이 들어옵니다.
        this.content = content;
        this.anonymousYn = (anonymousYn != null) ? anonymousYn : "N"; // 기본값 N
        this.status = CommentStatus.ACTIVE;
    }

    //댓글 수정/삭제용 내부 관절 (Dirty Checking) ---
    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
    }
}