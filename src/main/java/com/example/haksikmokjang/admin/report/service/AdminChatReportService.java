package com.example.haksikmokjang.admin.report.service;

import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.report.domain.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminChatReportService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    // 채팅 메시지 신고 상세 조회
    public AdminReportDetailResponse getChatMessageReportDetail(Report report) {
        ChatMessage chatMessage = chatMessageRepository.findById(report.getTargetId())
                .orElse(null);

        if (chatMessage == null) {
            return AdminReportDetailResponse.from(
                    report,
                    "존재하지 않는 채팅 메시지",
                    "신고 대상 채팅 메시지가 삭제되었거나 존재하지 않습니다.",
                    null,
                    "NOT_FOUND"
            );
        }

        return AdminReportDetailResponse.from(
                report,
                "채팅 메시지",
                chatMessage.getMessage(),
                chatMessage.getSender().getLoginId(),
                chatMessage.isDeleted() ? "HIDDEN" : "ACTIVE"
        );
    }

    // 채팅 상대 신고 상세 조회
    public AdminReportDetailResponse getChatMemberReportDetail(Report report) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findById(report.getTargetId())
                .orElse(null);

        if (chatRoomMember == null) {
            return AdminReportDetailResponse.from(
                    report,
                    "존재하지 않는 채팅 상대",
                    "신고 대상 채팅방 참여자가 삭제되었거나 존재하지 않습니다.",
                    null,
                    "NOT_FOUND"
            );
        }

        return AdminReportDetailResponse.from(
                report,
                "채팅 상대",
                "채팅방: " + chatRoomMember.getChatRoom().getRoomName(),
                chatRoomMember.getMember().getLoginId(),
                "ACTIVE"
        );
    }

    // 채팅 메시지 숨김 처리
    @Transactional
    public void hideChatMessage(Report report) {
        ChatMessage chatMessage = chatMessageRepository.findById(report.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        chatMessage.delete();
    }

    // 채팅 메시지 작성자 조회
    public Member findChatMessageWriter(Report report) {
        return chatMessageRepository.findById(report.getTargetId())
                .map(ChatMessage::getSender)
                .orElse(null);
    }

    // 채팅 상대 신고 대상 회원 조회
    public Member findChatMemberWriter(Report report) {
        return chatRoomMemberRepository.findById(report.getTargetId())
                .map(ChatRoomMember::getMember)
                .orElse(null);
    }
}