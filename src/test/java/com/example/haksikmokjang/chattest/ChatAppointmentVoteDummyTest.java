package com.example.haksikmokjang.chattest;

import com.example.haksikmokjang.chat.chatform.domain.ChatFormOptionType;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import com.example.haksikmokjang.chat.chatform.domain.ChatPlaceSource;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormAnswerRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormCreateRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormOptionRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormOptionResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormResultResponse;
import com.example.haksikmokjang.chat.chatform.service.ChatFormService;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * [약속 장소/시간 투표 테스트]
 *
 * 목적:
 * - 약속 투표의 장소 후보, 시간 후보, 장소 투표, 시간 투표, 결과, 수동 종료를 확인한다.
 *
 * 실행 전 조건:
 * - member_id 7, 8 유저가 있어야 한다.
 * - owner_profile_id 8, member_id 32 오너가 있어야 한다.
 * - store 테이블에 member_id 32 오너의 식당 데이터가 있어야 한다.
 *
 * 핵심 확인:
 * - 장소/시간을 둘 다 투표해도 자동 종료되면 안 된다.
 * - closeForm()을 호출했을 때만 종료되어야 한다.
 */
@SpringBootTest
public class ChatAppointmentVoteDummyTest {

    @Autowired
    private ChatRoomCreateService chatRoomCreateService;

    @Autowired
    private ChatFormService chatFormService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OwnerProfileRepository ownerProfileRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Commit
    @Transactional
    @DisplayName("약속 장소/시간 투표 생성/응답/결과/수동 종료")
    void appointmentVoteFullFlow() {
        Member user7 = memberRepository.findById(7L)
                .orElseThrow(() -> new RuntimeException("member_id 7 유저가 없습니다."));
        Member user8 = memberRepository.findById(8L)
                .orElseThrow(() -> new RuntimeException("member_id 8 유저가 없습니다."));
        OwnerProfile ownerProfile = ownerProfileRepository.findById(8L)
                .orElseThrow(() -> new RuntimeException("owner_profile_id 8 점주 프로필이 없습니다."));

        if (!ownerProfile.getMember().getMemberId().equals(32L)) {
            throw new RuntimeException("owner_profile_id 8의 member_id가 32번이 아닙니다.");
        }

        Store store = storeRepository.findAll().stream()
                .filter(findStore -> findStore.getMember() != null)
                .filter(findStore -> findStore.getMember().getMemberId().equals(32L))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("member_id 32 오너의 식당 데이터가 없습니다."));

        ChatRoomResponse room = chatRoomCreateService.createMealChatRoom(
                user7.getMemberId(),
                user8.getMemberId()
        );

        ChatMessageResponse formMessage = chatFormService.createForm(
                room.getChatRoomId(),
                ChatFormCreateRequest.builder()
                        .formType(ChatFormType.PLACE)
                        .title("약속 장소 시간 투표 테스트")
                        .build(),
                user7
        );

        Long formId = formMessage.getFormId();
        LocalDateTime appointmentAt = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).withSecond(0).withNano(0);

        chatFormService.addOption(formId, ChatFormOptionRequest.builder()
                .optionType(ChatFormOptionType.PLACE)
                .placeSource(ChatPlaceSource.STORE)
                .storeId(store.getStoreId())
                .placeName(store.getName())
                .optionText(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .build(), user7);

        chatFormService.addOption(formId, ChatFormOptionRequest.builder()
                .optionType(ChatFormOptionType.PLACE)
                .placeSource(ChatPlaceSource.CUSTOM)
                .placeName("직접 추가 장소")
                .optionText("직접 추가 장소")
                .address("서울 노원구 테스트 주소")
                .latitude(37.6250)
                .longitude(127.0630)
                .build(), user8);

        chatFormService.addOption(formId, ChatFormOptionRequest.builder()
                .optionType(ChatFormOptionType.TIME)
                .optionText("테스트 시간")
                .appointmentAt(appointmentAt)
                .memo("저녁 6시")
                .build(), user7);

        ChatFormResponse form = chatFormService.getForm(formId, user7);

        Long storePlaceOptionId = form.getOptions().stream()
                .filter(option -> ChatFormOptionType.PLACE.name().equals(option.getOptionType()))
                .filter(option -> ChatPlaceSource.STORE.name().equals(option.getPlaceSource()))
                .findFirst()
                .map(ChatFormOptionResponse::getOptionId)
                .orElseThrow(() -> new RuntimeException("점주 식당 장소 후보를 찾을 수 없습니다."));

        Long timeOptionId = form.getOptions().stream()
                .filter(option -> ChatFormOptionType.TIME.name().equals(option.getOptionType()))
                .findFirst()
                .map(ChatFormOptionResponse::getOptionId)
                .orElseThrow(() -> new RuntimeException("시간 후보를 찾을 수 없습니다."));

        chatFormService.answerForm(formId, ChatFormAnswerRequest.builder().optionId(storePlaceOptionId).build(), user7);
        chatFormService.answerForm(formId, ChatFormAnswerRequest.builder().optionId(timeOptionId).build(), user7);
        chatFormService.answerForm(formId, ChatFormAnswerRequest.builder().optionId(storePlaceOptionId).build(), user8);
        ChatFormResponse afterAllVoted = chatFormService.answerForm(formId, ChatFormAnswerRequest.builder().optionId(timeOptionId).build(), user8);

        assertEquals("N", afterAllVoted.getClosedYn(), "장소/시간 전원 투표해도 자동 종료되면 안 됩니다.");

        ChatFormResultResponse result = chatFormService.getResult(formId, user7);
        assertEquals(2, result.getPlaceVoteCount());
        assertEquals(2, result.getTimeVoteCount());

        ChatFormResponse closedForm = chatFormService.closeForm(formId, user7);
        assertEquals("Y", closedForm.getClosedYn());
        assertNotNull(formMessage.getChatMessageId());

        System.out.println("✅ 약속 장소/시간 투표 테스트 완료");
        System.out.println("ownerProfileId = " + ownerProfile.getOwnerProfileId());
        System.out.println("ownerMemberId = " + ownerProfile.getMember().getMemberId());
        System.out.println("storeId = " + store.getStoreId());
        System.out.println("roomId = " + room.getChatRoomId());
        System.out.println("formId = " + formId);
    }
}
