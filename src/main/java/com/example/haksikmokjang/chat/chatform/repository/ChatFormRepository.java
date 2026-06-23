package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatFormRepository extends JpaRepository<ChatForm, Long> {
    List<ChatForm> findAllByChatRoomAndReservationIdIsNotNullOrderByUpdatedAtDesc(ChatRoom chatRoom);
}