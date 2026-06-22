package com.example.haksikmokjang.chattest;

import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * [채팅방 생성 테스트]
 *
 * 목적:
 * - DB에 이미 존재하는 member_id 7, 8번 유저로 채팅방 3종을 생성한다.
 *
 * 테스트 기능:
 * 1. 학식메이트 채팅방 생성
 * 2. 과팅 채팅방 생성
 * 3. 소개팅 채팅방 생성
 *
 * 실행 후 확인:
 * - chat_room 테이블에 MEAL, GROUP_DATE, BLIND_DATE 방이 생성됐는지 확인
 * - chat_room_member 테이블에 7번, 8번이 참여자로 들어갔는지 확인
 */
@SpringBootTest
public class ChatRoomCreateDummyTest {

    @Autowired
    private ChatRoomCreateService chatRoomCreateService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Commit
    @Transactional
    @DisplayName("7번/8번 유저로 채팅방 3종 생성")
    void createBasicChatRoomsWithUser7And8() {
        Member user7 = memberRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("member_id 7 유저가 없습니다."));
        Member user8 = memberRepository.findById(8L)
                .orElseThrow(() -> new RuntimeException("member_id 8 유저가 없습니다."));

        ChatRoomResponse mealRoom = chatRoomCreateService.createMealChatRoom(
                user7.getMemberId(),
                user8.getMemberId()
        );

        ChatRoomResponse groupDateRoom = chatRoomCreateService.createGroupDateChatRoom(
                "7번 8번 과팅 테스트방",
                List.of(user7.getMemberId(), user8.getMemberId())
        );

        ChatRoomResponse blindDateRoom = chatRoomCreateService.createBlindDateChatRoom(
                user7.getMemberId(),
                user8.getMemberId()
        );

        assertNotNull(mealRoom.getChatRoomId());
        assertNotNull(groupDateRoom.getChatRoomId());
        assertNotNull(blindDateRoom.getChatRoomId());

        System.out.println("✅ 채팅방 3종 생성 완료");
        System.out.println("학식메이트 roomId = " + mealRoom.getChatRoomId());
        System.out.println("과팅 roomId = " + groupDateRoom.getChatRoomId());
        System.out.println("소개팅 roomId = " + blindDateRoom.getChatRoomId());
    }
}
