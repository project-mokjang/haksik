package com.example.haksikmokjang.chattest;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormCreateRequest;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormRepository;
import com.example.haksikmokjang.chat.chatform.service.ChatFormService;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ReviewReadyChatRoomDummyTest {
    @Autowired
    private ChatRoomCreateService chatRoomCreateService;
    @Autowired
    private ChatFormService chatFormService;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatFormRepository chatFormRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("pageuser01이 ownerpage01 식당 리뷰를 쓸 수 있는 종료 채팅방 3종 생성")
    void createClosedChatRoomsReadyForOwner01StoreReview() {
        Member user01 = getMemberByLoginId("pageuser01");
        Member user02 = getMemberByLoginId("pageuser02");
        Member owner01 = getMemberByLoginId("ownerpage01");

        Store owner01Store = getOrCreateOwner01Store(owner01);

        ChatRoomResponse mealRoom = chatRoomCreateService.createMealChatRoom(
                user01.getMemberId(),
                user02.getMemberId()
        );

        ChatRoomResponse groupDateRoom = chatRoomCreateService.createGroupDateChatRoom(
                "pageuser01 pageuser02 과팅 식당 리뷰 테스트방",
                List.of(user01.getMemberId(), user02.getMemberId())
        );

        ChatRoomResponse blindDateRoom = chatRoomCreateService.createBlindDateChatRoom(
                user01.getMemberId(),
                user02.getMemberId()
        );

        Long mealReservationId = makeStoreReviewReadyChatRoom(
                mealRoom.getChatRoomId(),
                user01,
                owner01Store,
                "학식메이트"
        );

        Long groupDateReservationId = makeStoreReviewReadyChatRoom(
                groupDateRoom.getChatRoomId(),
                user01,
                owner01Store,
                "과팅"
        );

        Long blindDateReservationId = makeStoreReviewReadyChatRoom(
                blindDateRoom.getChatRoomId(),
                user01,
                owner01Store,
                "소개팅"
        );

        assertNotNull(mealReservationId);
        assertNotNull(groupDateReservationId);
        assertNotNull(blindDateReservationId);

        System.out.println("✅ ownerpage01 식당 리뷰 가능한 종료 채팅방 3종 생성 완료");
        System.out.println("리뷰 작성 유저 = " + user01.getLoginId() + ", memberId = " + user01.getMemberId());
        System.out.println("상대 유저 = " + user02.getLoginId() + ", memberId = " + user02.getMemberId());
        System.out.println("점주 = " + owner01.getLoginId() + ", memberId = " + owner01.getMemberId());
        System.out.println("storeId = " + owner01Store.getStoreId());
        System.out.println("storeName = " + owner01Store.getName());

        System.out.println("학식메이트 roomId = " + mealRoom.getChatRoomId() + ", reservationId = " + mealReservationId);
        System.out.println("과팅 roomId = " + groupDateRoom.getChatRoomId() + ", reservationId = " + groupDateReservationId);
        System.out.println("소개팅 roomId = " + blindDateRoom.getChatRoomId() + ", reservationId = " + blindDateReservationId);

        System.out.println("➡ pageuser01로 로그인 후 리뷰쓰기 페이지에서 식당 리뷰 3개가 떠야 합니다.");
    }

    private Long makeStoreReviewReadyChatRoom(
            Long chatRoomId,
            Member reservationMember,
            Store store,
            String label
    ) {
        ChatMessageResponse formMessage = chatFormService.createForm(
                chatRoomId,
                ChatFormCreateRequest.builder()
                        .formType(ChatFormType.PLACE)
                        .title(label + " 식당 리뷰 테스트 약속")
                        .build(),
                reservationMember
        );

        Long formId = formMessage.getFormId();

        ChatForm chatForm = chatFormRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("chat_form 없음: " + formId));

        LocalDateTime reservationAt = LocalDateTime.now()
                .minusHours(2)
                .withSecond(0)
                .withNano(0);

        Reservation reservation = Reservation.builder()
                .member(reservationMember)
                .store(store)
                .reservationAt(reservationAt)
                .peopleCount(2)
                .requestMemo("[식당 리뷰 가능 더미] " + label + " / chatRoomId=" + chatRoomId + " / formId=" + formId)
                .status(ReservationStatus.COMPLETED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        chatForm.connectReservation(savedReservation.getReservationId());
        chatForm.close();

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("chat_room 없음: " + chatRoomId));

        chatRoom.confirmAppointment(reservationAt, reservationAt.plusHours(1));
        chatRoom.close();

        return savedReservation.getReservationId();
    }

    private Store getOrCreateOwner01Store(Member owner01) {
        return storeRepository.findAll().stream()
                .filter(store -> store.getMember() != null)
                .filter(store -> "ownerpage01".equals(store.getMember().getLoginId()))
                .findFirst()
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .member(owner01)
                                .name("광운대역 500m 돈까스")
                                .category("일식")
                                .address("서울특별시 노원구 광운로 20")
                                .latitude(37.6250)
                                .longitude(127.0630)
                                .phone("02-111-1111")
                                .operatingHours("10:00 - 22:00")
                                .businessStatus(BusinessStatus.OPEN)
                                .build()
                ));
    }

    private Member getMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException(loginId + " 계정이 없습니다."));
    }
}