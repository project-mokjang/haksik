package com.example.haksikmokjang.chat.chatmessage.repository;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageEditHistoryRepository extends JpaRepository<ChatMessageEditHistory, Long> {
    List<ChatMessageEditHistory> findAllByChatMessageOrderByCreatedAtDesc(ChatMessage chatMessage);
}