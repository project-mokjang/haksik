package com.example.haksikmokjang.chat.chatreview.domain;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "CHAT_REVIEW",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_CHAT_REVIEW_ROOM_REVIEWER_TARGET",
                        columnNames = {"chat_room_id", "reviewer_member_id", "target_member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_review_id")
    private Long chatReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_member_id", nullable = false)
    private Member reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private Member targetMember;

    @Column(name = "manner_score", nullable = false)
    private Integer mannerScore;

    @Column(name = "no_show_yn", nullable = false, length = 1)
    private String noShowYn;

    @Column(name = "content", length = 500)
    private String content;

    public ChatReview(
            ChatRoom chatRoom,
            Member reviewer,
            Member targetMember,
            Integer mannerScore,
            boolean noShow,
            String content
    ) {
        this.chatRoom = chatRoom;
        this.reviewer = reviewer;
        this.targetMember = targetMember;
        this.mannerScore = mannerScore;
        this.noShowYn = noShow ? "Y" : "N";
        this.content = content;
    }

    public boolean isNoShow() {
        return "Y".equals(this.noShowYn);
    }
}
