package com.example.haksikmokjang.chat.chatform.service;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormAnswer;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOption;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import com.example.haksikmokjang.chat.chatform.domain.ChatPlaceSource;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormAnswerRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormCreateRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormOptionRequest;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormOptionResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormOptionResultResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormResultResponse;
import com.example.haksikmokjang.chat.chatform.dto.ChatNearbyStoreResponse;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormAnswerRepository;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormOptionRepository;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormRepository;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatMessageService;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatFormService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatFormRepository chatFormRepository;
    private final ChatFormOptionRepository chatFormOptionRepository;
    private final ChatFormAnswerRepository chatFormAnswerRepository;
    private final ChatMessageService chatMessageService;
    private final StoreRepository storeRepository;
    private final UserProfileRepository userProfileRepository;

    // 폼 생성 후 채팅방에 FORM 메시지까지 저장
    @Transactional
    public ChatMessageResponse createForm(Long chatRoomId, ChatFormCreateRequest request, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        checkChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        ChatFormType formType = request.getFormType() == null ? ChatFormType.VOTE : request.getFormType();
        String title = getRequiredText(request.getTitle(), "폼 제목");

        ChatForm chatForm = chatFormRepository.save(new ChatForm(
                chatRoom,
                loginMember,
                formType,
                title
        ));

        if (request.getOptions() != null) {
            int optionOrder = 1;

            for (ChatFormOptionRequest optionRequest : request.getOptions()) {
                ChatFormOption option = createOptionEntity(
                        chatForm,
                        loginMember,
                        optionRequest,
                        optionOrder
                );

                chatFormOptionRepository.save(option);
                optionOrder++;
            }
        }

        return chatMessageService.sendFormMessage(
                chatRoomId,
                chatForm.getChatFormId(),
                getFormCardMessage(chatForm),
                loginMember
        );
    }

    // 폼 상세 조회
    public ChatFormResponse getForm(Long formId, Member loginMember) {
        ChatForm chatForm = getChatForm(formId);
        checkChatRoomMember(chatForm.getChatRoom(), loginMember);

        List<ChatFormOption> options = chatFormOptionRepository.findAllByChatFormOrderByOptionOrderAsc(chatForm);
        Optional<ChatFormAnswer> myAnswer = chatFormAnswerRepository.findByChatFormAndMember(chatForm, loginMember);
        Long mySelectedOptionId = myAnswer
                .map(answer -> answer.getChatFormOption().getChatFormOptionId())
                .orElse(null);

        List<ChatFormOptionResponse> optionResponses = options.stream()
                .map(option -> createOptionResponse(option, mySelectedOptionId))
                .toList();

        return ChatFormResponse.builder()
                .formId(chatForm.getChatFormId())
                .chatRoomId(chatForm.getChatRoom().getChatRoomId())
                .creatorId(chatForm.getCreator().getMemberId())
                .formType(chatForm.getFormType().name())
                .title(chatForm.getTitle())
                .closedYn(chatForm.getClosedYn())
                .mySelectedOptionId(mySelectedOptionId)
                .optionCount(options.size())
                .answerCount(chatFormAnswerRepository.countByChatForm(chatForm))
                .options(optionResponses)
                .createdAt(chatForm.getCreatedAt())
                .build();
    }

    // 장소 투표 폼에 후보 추가
    @Transactional
    public ChatFormOptionResponse addOption(Long formId, ChatFormOptionRequest request, Member loginMember) {
        ChatForm chatForm = getChatForm(formId);
        checkChatRoomMember(chatForm.getChatRoom(), loginMember);

        if (chatForm.isClosed()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!chatForm.isPlaceForm()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int optionOrder = chatFormOptionRepository.countByChatForm(chatForm) + 1;

        ChatFormOption option = createOptionEntity(
                chatForm,
                loginMember,
                request,
                optionOrder
        );

        ChatFormOption savedOption = chatFormOptionRepository.save(option);

        return createOptionResponse(savedOption, null);
    }

    // 응답 제출 또는 수정
    @Transactional
    public ChatFormResponse answerForm(Long formId, ChatFormAnswerRequest request, Member loginMember) {
        ChatForm chatForm = getChatForm(formId);
        checkChatRoomMember(chatForm.getChatRoom(), loginMember);

        if (chatForm.isClosed()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getOptionId() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ChatFormOption selectedOption = chatFormOptionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        if (!selectedOption.getChatForm().getChatFormId().equals(chatForm.getChatFormId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        chatFormAnswerRepository.findByChatFormAndMember(chatForm, loginMember)
                .ifPresentOrElse(
                        answer -> answer.changeOption(selectedOption),
                        () -> chatFormAnswerRepository.save(new ChatFormAnswer(chatForm, selectedOption, loginMember))
                );

        return getForm(formId, loginMember);
    }

    // 결과 조회
    public ChatFormResultResponse getResult(Long formId, Member loginMember) {
        ChatForm chatForm = getChatForm(formId);
        checkChatRoomMember(chatForm.getChatRoom(), loginMember);

        List<ChatFormOption> options = chatFormOptionRepository.findAllByChatFormOrderByOptionOrderAsc(chatForm);
        List<ChatFormAnswer> answers = chatFormAnswerRepository.findAllByChatForm(chatForm);
        Map<Long, Long> voteCountMap = answers.stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.getChatFormOption().getChatFormOptionId(),
                        Collectors.counting()
                ));

        Long mySelectedOptionId = chatFormAnswerRepository.findByChatFormAndMember(chatForm, loginMember)
                .map(answer -> answer.getChatFormOption().getChatFormOptionId())
                .orElse(null);

        List<ChatFormOptionResultResponse> results = options.stream()
                .map(option -> ChatFormOptionResultResponse.builder()
                        .optionId(option.getChatFormOptionId())
                        .optionText(option.getOptionText())
                        .placeSource(option.getPlaceSource() == null ? null : option.getPlaceSource().name())
                        .storeId(option.getStoreId())
                        .placeName(option.getPlaceName())
                        .address(option.getAddress())
                        .latitude(option.getLatitude())
                        .longitude(option.getLongitude())
                        .voteCount(voteCountMap.getOrDefault(option.getChatFormOptionId(), 0L).intValue())
                        .selectedByMe(option.getChatFormOptionId().equals(mySelectedOptionId))
                        .build())
                .toList();

        return ChatFormResultResponse.builder()
                .formId(chatForm.getChatFormId())
                .formType(chatForm.getFormType().name())
                .title(chatForm.getTitle())
                .totalVoteCount(answers.size())
                .mySelectedOptionId(mySelectedOptionId)
                .results(results)
                .build();
    }

    // 지도에 띄울 주변 점주 가게 조회
    public List<ChatNearbyStoreResponse> getNearbyStores(Double lat, Double lng, Double radius) {
        Double safeRadius = radius == null ? 3.0 : radius;

        return storeRepository.findNearbyStores(lat, lng, safeRadius).stream()
                .map(store -> ChatNearbyStoreResponse.builder()
                        .storeId(store.getStoreId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .category(store.getCategory())
                        .latitude(store.getLatitude())
                        .longitude(store.getLongitude())
                        .businessStatus(store.getBusinessStatus() == null ? null : store.getBusinessStatus().name())
                        .build())
                .toList();
    }

    private ChatFormOption createOptionEntity(
            ChatForm chatForm,
            Member loginMember,
            ChatFormOptionRequest request,
            int optionOrder
    ) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String optionText = request.getOptionText();
        ChatPlaceSource placeSource = request.getPlaceSource();
        Long storeId = request.getStoreId();
        String placeName = request.getPlaceName();
        String address = request.getAddress();
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        String mapUrl = request.getMapUrl();

        if (chatForm.getFormType() == ChatFormType.VOTE) {
            optionText = getRequiredText(optionText, "선택지");
            placeSource = null;
            storeId = null;
            placeName = null;
            address = null;
            latitude = null;
            longitude = null;
            mapUrl = null;
        } else {
            if (placeSource == null) {
                placeSource = ChatPlaceSource.CUSTOM;
            }

            if (placeSource == ChatPlaceSource.STORE && storeId != null) {
                Store store = storeRepository.findById(storeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

                placeName = store.getName();
                address = store.getAddress();
                latitude = store.getLatitude();
                longitude = store.getLongitude();
                optionText = store.getName();
            } else {
                placeSource = ChatPlaceSource.CUSTOM;
                placeName = getRequiredText(placeName, "장소명");
                optionText = getBlankToDefault(optionText, placeName);
            }
        }

        return new ChatFormOption(
                chatForm,
                loginMember,
                optionText,
                optionOrder,
                placeSource,
                storeId,
                placeName,
                address,
                latitude,
                longitude,
                mapUrl
        );
    }

    private ChatFormOptionResponse createOptionResponse(ChatFormOption option, Long mySelectedOptionId) {
        return ChatFormOptionResponse.builder()
                .optionId(option.getChatFormOptionId())
                .optionText(option.getOptionText())
                .optionOrder(option.getOptionOrder())
                .placeSource(option.getPlaceSource() == null ? null : option.getPlaceSource().name())
                .storeId(option.getStoreId())
                .placeName(option.getPlaceName())
                .address(option.getAddress())
                .latitude(option.getLatitude())
                .longitude(option.getLongitude())
                .mapUrl(option.getMapUrl())
                .createdByMemberId(option.getCreatedByMember().getMemberId())
                .createdByNickname(getNickname(option.getCreatedByMember()))
                .selectedByMe(option.getChatFormOptionId().equals(mySelectedOptionId))
                .build();
    }

    private String getFormCardMessage(ChatForm chatForm) {
        if (chatForm.getFormType() == ChatFormType.PLACE) {
            return "[장소 투표] " + chatForm.getTitle();
        }

        return "[투표] " + chatForm.getTitle();
    }

    private ChatForm getChatForm(Long formId) {
        return chatFormRepository.findById(formId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void checkChatRoomMember(ChatRoom chatRoom, Member member) {
        if (!chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, member)) {
            throw new CustomException(ErrorCode.CHAT_MEMBER_NOT_FOUND);
        }
    }

    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }

    private String getRequiredText(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return text.trim();
    }

    private String getBlankToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }
}
