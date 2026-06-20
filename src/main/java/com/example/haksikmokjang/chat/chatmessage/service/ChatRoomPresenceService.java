package com.example.haksikmokjang.chat.chatmessage.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatRoomPresenceService {

    // 채팅방별 현재 보고 있는 회원 loginId 목록
    private final Map<Long, Set<String>> chatRoomWatchingMembers = new ConcurrentHashMap<>();

    // WebSocket 세션별 현재 보고 있는 채팅방 정보
    private final Map<String, ChatRoomPresence> sessionPresenceMap = new ConcurrentHashMap<>();

    // 채팅방 입장 처리
    public void enterChatRoom(String sessionId, String loginId, Long chatRoomId) {
        if (sessionId == null || loginId == null || chatRoomId == null) {
            return;
        }

        // 같은 세션이 기존에 보고 있던 채팅방이 있으면 먼저 제거
        leaveChatRoom(sessionId);

        sessionPresenceMap.put(sessionId, new ChatRoomPresence(chatRoomId, loginId));

        chatRoomWatchingMembers
                .computeIfAbsent(chatRoomId, key -> ConcurrentHashMap.newKeySet())
                .add(loginId);
    }

    // 채팅방 나감 처리
    public void leaveChatRoom(String sessionId) {
        if (sessionId == null) {
            return;
        }

        ChatRoomPresence presence = sessionPresenceMap.remove(sessionId);

        if (presence == null) {
            return;
        }

        Set<String> watchingMembers = chatRoomWatchingMembers.get(presence.chatRoomId);

        if (watchingMembers == null) {
            return;
        }

        watchingMembers.remove(presence.loginId);

        if (watchingMembers.isEmpty()) {
            chatRoomWatchingMembers.remove(presence.chatRoomId);
        }
    }

    // 해당 회원이 현재 그 채팅방을 보고 있는지 확인
    public boolean isWatchingChatRoom(Long chatRoomId, String loginId) {
        if (chatRoomId == null || loginId == null) {
            return false;
        }

        Set<String> watchingMembers = chatRoomWatchingMembers.get(chatRoomId);

        if (watchingMembers == null) {
            return false;
        }

        return watchingMembers.contains(loginId);
    }

    private static class ChatRoomPresence {

        private final Long chatRoomId;
        private final String loginId;

        private ChatRoomPresence(Long chatRoomId, String loginId) {
            this.chatRoomId = chatRoomId;
            this.loginId = loginId;
        }
    }
}