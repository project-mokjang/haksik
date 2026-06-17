package com.example.haksikmokjang.chat.repository;

import com.example.haksikmokjang.chat.domain.ChatMessage;
import com.example.haksikmokjang.chat.domain.ChatMessageEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageEditHistoryRepository extends JpaRepository<ChatMessageEditHistory, Long> {
    List<ChatMessageEditHistory> findAllByChatMessageOrderByCreatedAtDesc(ChatMessage chatMessage);
}