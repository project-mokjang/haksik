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

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_mode", nullable = false, length = 20)
    private ChatMatchingMode matchingMode;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false, length = 20)
    private ChatRoomStatus roomStatus;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "appointment_at")
    private LocalDateTime appointmentAt;

    @Column(name = "auto_close_at")
    private LocalDateTime autoCloseAt;

    public ChatRoom(ChatRoomType roomType, ChatMatchingMode matchingMode, String roomName) {
        this.roomType = roomType;
        this.matchingMode = matchingMode;
        this.roomName = roomName;
        this.roomStatus = ChatRoomStatus.ACTIVE;
    }

    public void updateLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageAt = LocalDateTime.now();
    }

    public void confirmAppointment(LocalDateTime appointmentAt, LocalDateTime autoCloseAt) {
        this.appointmentAt = appointmentAt;
        this.autoCloseAt = autoCloseAt;
    }

    public void close() {
        this.roomStatus = ChatRoomStatus.CLOSED;
    }

    public boolean isActive() {
        return this.roomStatus == ChatRoomStatus.ACTIVE;
    }
}
