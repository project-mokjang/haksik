package com.example.haksikmokjang.chat.chatmessage.controller;

import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketDeleteRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketEditRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketEventType;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketMessageRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketReadRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatMessageService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatMessageService chatMessageService;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 메시지 전송
    @MessageMapping("/chat/message/send")
    public void sendMessage(
            ChatSocketMessageRequest request,
            Principal principal
    ) {
        Member loginMember = getLoginMember(principal);

        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
                .message(request.getMessage())
                .build();

        ChatMessageResponse message = chatMessageService.sendMessage(
                request.getChatRoomId(),
                messageRequest,
                loginMember
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + request.getChatRoomId(),
                ChatSocketResponse.builder()
                        .eventType(ChatSocketEventType.SEND)
                        .chatRoomId(request.getChatRoomId())
                        .chatMessageId(message.getChatMessageId())
                        .message(message)
                        .build()
        );
    }

    // 메시지 수정
    @MessageMapping("/chat/message/edit")
    public void editMessage(
            ChatSocketEditRequest request,
            Principal principal
    ) {
        Member loginMember = getLoginMember(principal);

        Long chatRoomId = chatMessageService.getChatRoomIdByMessageId(request.getChatMessageId());

        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
                .message(request.getMessage())
                .build();

        ChatMessageResponse message = chatMessageService.updateMessage(
                request.getChatMessageId(),
                messageRequest,
                loginMember
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + chatRoomId,
                ChatSocketResponse.builder()
                        .eventType(ChatSocketEventType.EDIT)
                        .chatRoomId(chatRoomId)
                        .chatMessageId(message.getChatMessageId())
                        .message(message)
                        .build()
        );
    }

    // 메시지 삭제
    @MessageMapping("/chat/message/delete")
    public void deleteMessage(
            ChatSocketDeleteRequest request,
            Principal principal
    ) {
        Member loginMember = getLoginMember(principal);

        Long chatRoomId = chatMessageService.getChatRoomIdByMessageId(request.getChatMessageId());

        ChatMessageResponse message = chatMessageService.deleteMessage(
                request.getChatMessageId(),
                loginMember
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + chatRoomId,
                ChatSocketResponse.builder()
                        .eventType(ChatSocketEventType.DELETE)
                        .chatRoomId(chatRoomId)
                        .chatMessageId(message.getChatMessageId())
                        .message(message)
                        .build()
        );
    }

    // 읽음 처리
    @MessageMapping("/chat/message/read")
    public void readMessage(
            ChatSocketReadRequest request,
            Principal principal
    ) {
        Member loginMember = getLoginMember(principal);

        chatMessageService.readMessages(
                request.getChatRoomId(),
                request.getLastReadMessageId(),
                loginMember
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + request.getChatRoomId(),
                ChatSocketResponse.builder()
                        .eventType(ChatSocketEventType.READ)
                        .chatRoomId(request.getChatRoomId())
                        .readerMemberId(loginMember.getMemberId())
                        .lastReadMessageId(request.getLastReadMessageId())
                        .build()
        );
    }

    private Member getLoginMember(Principal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return memberRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
