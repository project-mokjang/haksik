package com.example.haksikmokjang.chat.chatmessage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ChatWebSocketPresenceEventListener {

    private static final String CHAT_ROOM_SUBSCRIBE_PREFIX = "/sub/chat/rooms/";

    private final ChatRoomPresenceService chatRoomPresenceService;

    // 채팅방 WebSocket 구독 시 현재 보고 있는 채팅방으로 등록
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(CHAT_ROOM_SUBSCRIBE_PREFIX)) {
            return;
        }

        Principal principal = accessor.getUser();

        if (principal == null) {
            return;
        }

        Long chatRoomId = getChatRoomId(destination);
        String sessionId = accessor.getSessionId();

        chatRoomPresenceService.enterChatRoom(
                sessionId,
                principal.getName(),
                chatRoomId
        );
    }

    // WebSocket 구독 해제 시 현재 보고 있는 채팅방에서 제거
    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        chatRoomPresenceService.leaveChatRoom(accessor.getSessionId());
    }

    // WebSocket 연결 종료 시 현재 보고 있는 채팅방에서 제거
    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        chatRoomPresenceService.leaveChatRoom(accessor.getSessionId());
    }

    // 구독 주소에서 채팅방 ID 추출
    private Long getChatRoomId(String destination) {
        String chatRoomIdText = destination.substring(CHAT_ROOM_SUBSCRIBE_PREFIX.length());

        return Long.parseLong(chatRoomIdText);
    }
}