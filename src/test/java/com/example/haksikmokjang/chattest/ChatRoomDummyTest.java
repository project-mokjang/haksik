package com.example.haksikmokjang.chattest;

import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ChatRoomDummyTest {
    @Autowired
    private ChatRoomCreateService chatRoomCreateService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("pageuser01/pageuser02 채팅방 3종 생성")
    void createThreeTypeChatRooms() {
        Member user01 = getMemberByLoginId("user1");
        Member user02 = getMemberByLoginId("user2");

        ChatRoomResponse mealRoom = chatRoomCreateService.createMealChatRoom(
                user01.getMemberId(),
                user02.getMemberId()
        );

        ChatRoomResponse groupDateRoom = chatRoomCreateService.createGroupDateChatRoom(
                "pageuser01 pageuser02 과팅 테스트방",
                List.of(user01.getMemberId(), user02.getMemberId())
        );

        ChatRoomResponse blindDateRoom = chatRoomCreateService.createBlindDateChatRoom(
                user01.getMemberId(),
                user02.getMemberId()
        );

        assertNotNull(mealRoom.getChatRoomId());
        assertNotNull(groupDateRoom.getChatRoomId());
        assertNotNull(blindDateRoom.getChatRoomId());

        System.out.println("✅ pageuser01/pageuser02 채팅방 3종 생성 완료");
        System.out.println("학식메이트 roomId = " + mealRoom.getChatRoomId());
        System.out.println("과팅 roomId = " + groupDateRoom.getChatRoomId());
        System.out.println("소개팅 roomId = " + blindDateRoom.getChatRoomId());
    }

    private Member getMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException(loginId + " 계정이 없습니다."));
    }
}