package com.example.haksikmokjang.chattest;

import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewRequest;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewResponse;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewTargetResponse;
import com.example.haksikmokjang.chat.chatreview.service.ChatReviewService;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomDetailResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * [채팅방 종료 + 평가 테스트]
 *
 * 목적:
 * - 7번/8번 소개팅방을 종료하고, 종료된 방에서 상대 평가가 저장되는지 확인한다.
 *
 * 실행 전 조건:
 * - member_id 7, 8 유저가 있어야 한다.
 * - 7번/8번 유저는 UserProfile이 있어야 한다. 평가 시 신뢰 온도 갱신에 필요하다.
 *
 * 테스트 기능:
 * 1. 소개팅방 생성
 * 2. 채팅방 종료
 * 3. 평가 대상 조회
 * 4. 7번이 8번 평가
 * 5. 내 평가 목록 조회
 */
@SpringBootTest
public class ChatEndReviewDummyTest {

    @Autowired
    private ChatRoomCreateService chatRoomCreateService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatReviewService chatReviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Commit
    @Transactional
    @DisplayName("채팅방 종료 후 상대 평가 등록")
    void endChatRoomAndCreateReview() {
        Member user7 = memberRepository.findById(7L)
                .orElseThrow(() -> new RuntimeException("member_id 7 유저가 없습니다."));
        Member user8 = memberRepository.findById(8L)
                .orElseThrow(() -> new RuntimeException("member_id 8 유저가 없습니다."));

        ChatRoomResponse room = chatRoomCreateService.createBlindDateChatRoom(
                user7.getMemberId(),
                user8.getMemberId()
        );

        ChatRoomDetailResponse closedRoom = chatRoomService.endChatRoom(room.getChatRoomId(), user7);
        assertEquals("CLOSED", closedRoom.getRoomStatus().name());

        List<ChatReviewTargetResponse> targets = chatReviewService.getReviewTargets(room.getChatRoomId(), user7);
        assertFalse(targets.isEmpty());

        ChatReviewRequest request = new ChatReviewRequest();
        ReflectionTestUtils.setField(request, "targetMemberId", user8.getMemberId());
        ReflectionTestUtils.setField(request, "mannerScore", 5);
        ReflectionTestUtils.setField(request, "noShow", false);
        ReflectionTestUtils.setField(request, "content", "채팅 평가 테스트입니다.");

        ChatReviewResponse review = chatReviewService.createReview(
                room.getChatRoomId(),
                request,
                user7
        );

        List<ChatReviewResponse> myReviews = chatReviewService.getMyReviews(room.getChatRoomId(), user7);

        assertNotNull(review.getChatReviewId());
        assertEquals(1, myReviews.size());

        System.out.println("✅ 채팅방 종료/평가 테스트 완료");
        System.out.println("roomId = " + room.getChatRoomId());
        System.out.println("reviewId = " + review.getChatReviewId());
    }
}
