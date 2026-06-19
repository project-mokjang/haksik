package com.example.haksikmokjang.chat.chatroom.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 채팅방 타입: 1:1 채팅방 / 그룹 채팅방
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_mode", nullable = false, length = 20)
    private ChatMatchingMode matchingMode;

    // 그룹 채팅방 이름
    // 1:1 채팅방은 화면에서 상대방 닉네임으로 표시한다
    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    // 채팅방 상태: 대화 가능 / 종료
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false, length = 20)
    private ChatRoomStatus roomStatus;

    // 채팅방 목록에서 보여줄 마지막 메시지
    @Column(name = "last_message", length = 500)
    private String lastMessage;

    // 마지막 메시지가 작성된 시간
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    public ChatRoom(ChatRoomType roomType, ChatMatchingMode matchingMode, String roomName) {
        this.roomType = roomType;
        this.matchingMode = matchingMode;
        this.roomName = roomName;
        this.roomStatus = ChatRoomStatus.ACTIVE;
    }

    // 메시지 전송 시 채팅방의 마지막 메시지 정보 갱신
    public void updateLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageAt = LocalDateTime.now();
    }

    // 매칭 종료 또는 채팅 종료 시 채팅방 종료 처리
    public void close() {
        this.roomStatus = ChatRoomStatus.CLOSED;
    }

    // 현재 채팅방이 대화 가능한 상태인지 확인
    public boolean isActive() {
        return this.roomStatus == ChatRoomStatus.ACTIVE;
    }
}