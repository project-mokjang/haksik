package com.example.haksikmokjang.chat.chatroom.repository;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByRoomStatusAndAutoCloseAtLessThanEqual(
            ChatRoomStatus roomStatus,
            LocalDateTime now
    );
}
