package com.example.haksikmokjang.chat.repository;

import com.example.haksikmokjang.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}