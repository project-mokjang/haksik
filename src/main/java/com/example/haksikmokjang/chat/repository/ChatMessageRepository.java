package com.example.haksikmokjang.chat.repository;

import com.example.haksikmokjang.chat.domain.ChatMessage;
import com.example.haksikmokjang.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 메시지 목록 조회
    List<ChatMessage> findAllByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // 채팅방의 마지막 메시지 조회
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);
}