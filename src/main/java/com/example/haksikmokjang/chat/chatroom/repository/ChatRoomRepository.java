package com.example.haksikmokjang.chat.chatroom.repository;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}