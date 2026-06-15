package com.example.haksikmokjang.chat.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOM_MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_member_id")
    private Long chatRoomMemberId;

    // 참여 중인 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 채팅방 참여 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 대표자 / 일반 참여자
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatRoomMemberRole role;

    // 마지막으로 읽은 메시지 ID
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    // 마지막으로 채팅방을 확인한 시간
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public ChatRoomMember(ChatRoom chatRoom, Member member, ChatRoomMemberRole role) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.role = role;
    }

    // 채팅방 입장 시 마지막 읽은 메시지 갱신
    public void updateLastReadMessage(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = LocalDateTime.now();
    }

    // 대표자인지 확인
    public boolean isLeader() {
        return this.role == ChatRoomMemberRole.LEADER;
    }
}