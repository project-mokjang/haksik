package com.example.haksikmokjang.chat.chatform.service;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormAnswer;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOption;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOptionType;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import com.example.haksikmokjang.chat.chatform.domain.ChatPlaceSource;
import com.example.haksikmokjang.chat.chatform.dto.*;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormAnswerRepository;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormOptionRepository;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormRepository;
import com.example.haksikmokjang.chat.chatform.repository.ChatStoreReviewRepository;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatMessageService;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.ownerpage.store.domain.*;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationRequest;
import com.example.haksikmokjang.ownerpage.store.repository.MenuRepository;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.ownerpage.store.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatFormService {
    private static final int CHAT_ROOM_AUTO_CLOSE_AFTER_APPOINTMENT_HOURS = 1;
    private final ReservationService reservationService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatFormRepository chatFormRepository;
    private final ChatFormOptionRepository chatFormOptionRepository;
    private final ChatFormAnswerRepository chatFormAnswerRepository;
    private final ChatMessageService chatMessageService;
    private final StoreRepository storeRepository;
    private final UserProfileRepository userProfileRepository;
    private final MenuRepository menuRepository;
    private final ChatStoreReviewRepository chatStoreReviewRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ReservationRepository reservationRepository;
    private final StoreReviewRepository storeReviewRepository;

    // 폼 생성 후 채팅방에 FORM 메시지까지 저장
    @Transactional
    public ChatMessageResponse createForm(Long chatRoomId, ChatFormCreateRequest request, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        checkChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        ChatFormType formType = normalizeFormType(request.getFormType());
        String title = getRequiredText(request.getTitle());

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
        Map<ChatFormOptionType, Long> mySelectedOptionIds = getMySelectedOptionIds(chatForm, loginMember);

        List<ChatFormOptionResponse> optionResponses = options.stream()
                .map(option -> createOptionResponse(option, mySelectedOptionIds))
                .toList();

        Long mySelectedOptionId = mySelectedOptionIds.get(ChatFormOptionType.VOTE);
        Long mySelectedPlaceOptionId = mySelectedOptionIds.get(ChatFormOptionType.PLACE);
        Long mySelectedTimeOptionId = mySelectedOptionIds.get(ChatFormOptionType.TIME);

        return ChatFormResponse.builder()
                .formId(chatForm.getChatFormId())
                .chatRoomId(chatForm.getChatRoom().getChatRoomId())
                .creatorId(chatForm.getCreator().getMemberId())
                .formType(chatForm.getFormType().name())
                .title(chatForm.getTitle())
                .closedYn(chatForm.getClosedYn())
                .canCloseByMe(canCloseForm(chatForm, loginMember))
                .mySelectedOptionId(mySelectedOptionId)
                .mySelectedPlaceOptionId(mySelectedPlaceOptionId)
                .mySelectedTimeOptionId(mySelectedTimeOptionId)
                .optionCount(options.size())
                .answerCount(chatFormAnswerRepository.countByChatForm(chatForm))
                .placeAnswerCount(chatFormAnswerRepository.countByChatFormAndAnswerType(chatForm, ChatFormOptionType.PLACE))
                .timeAnswerCount(chatFormAnswerRepository.countByChatFormAndAnswerType(chatForm, ChatFormOptionType.TIME))
                .options(optionResponses)
                .createdAt(chatForm.getCreatedAt())
                .build();
    }

    // 지도 약속 폼에 장소 후보 또는 시간 후보 추가
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

        ChatFormOptionType optionType = getRequestOptionType(request);
        int optionOrder = chatFormOptionRepository.countByChatFormAndOptionType(chatForm, optionType) + 1;

        ChatFormOption option = createOptionEntity(
                chatForm,
                loginMember,
                request,
                optionOrder
        );

        ChatFormOption savedOption = chatFormOptionRepository.save(option);

        return createOptionResponse(savedOption, getMySelectedOptionIds(chatForm, loginMember));
    }

    // 폼 응답 제출 또는 수정
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

        ChatFormOptionType answerType = getOptionType(selectedOption, chatForm);

        chatFormAnswerRepository.findByChatFormAndMemberAndAnswerType(chatForm, loginMember, answerType)
                .ifPresentOrElse(
                        answer -> answer.changeOption(selectedOption),
                        () -> chatFormAnswerRepository.save(new ChatFormAnswer(chatForm, selectedOption, loginMember))
                );

        return getForm(formId, loginMember);
    }

    // 모든 폼 종료
    @Transactional
    public ChatFormResponse closeForm(Long formId, Member loginMember) {
        ChatForm chatForm = getChatForm(formId);
        checkChatRoomMember(chatForm.getChatRoom(), loginMember);

        if (!canCloseForm(chatForm, loginMember)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        chatForm.close();

        if (chatForm.isPlaceForm()) {
            confirmAppointmentByTimeVoteResult(chatForm);
            createReservationByVoteResult(chatForm, loginMember);
        }

        return getForm(formId, loginMember);
    }

    // 폼 결과 조회
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

        Map<ChatFormOptionType, Long> mySelectedOptionIds = getMySelectedOptionIds(chatForm, loginMember);

        List<ChatFormOptionResultResponse> results = options.stream()
                .map(option -> {
                    ChatFormOptionType optionType = getOptionType(option, chatForm);

                    return ChatFormOptionResultResponse.builder()
                            .optionId(option.getChatFormOptionId())
                            .optionType(optionType.name())
                            .optionText(option.getOptionText())
                            .placeSource(option.getPlaceSource() == null ? null : option.getPlaceSource().name())
                            .storeId(option.getStoreId())
                            .placeName(option.getPlaceName())
                            .address(option.getAddress())
                            .latitude(option.getLatitude())
                            .longitude(option.getLongitude())
                            .appointmentAt(option.getAppointmentAt())
                            .memo(option.getMemo())
                            .voteCount(voteCountMap.getOrDefault(option.getChatFormOptionId(), 0L).intValue())
                            .selectedByMe(option.getChatFormOptionId().equals(mySelectedOptionIds.get(optionType)))
                            .build();
                })
                .toList();

        return ChatFormResultResponse.builder()
                .formId(chatForm.getChatFormId())
                .formType(chatForm.getFormType().name())
                .title(chatForm.getTitle())
                .totalVoteCount(answers.size())
                .placeVoteCount(chatFormAnswerRepository.countByChatFormAndAnswerType(chatForm, ChatFormOptionType.PLACE))
                .timeVoteCount(chatFormAnswerRepository.countByChatFormAndAnswerType(chatForm, ChatFormOptionType.TIME))
                .mySelectedOptionId(mySelectedOptionIds.get(ChatFormOptionType.VOTE))
                .mySelectedPlaceOptionId(mySelectedOptionIds.get(ChatFormOptionType.PLACE))
                .mySelectedTimeOptionId(mySelectedOptionIds.get(ChatFormOptionType.TIME))
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

    // 지도 식당 상세 조회
    public ChatStoreDetailResponse getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        List<ChatStoreMenuResponse> menus = menuRepository.findByStore_StoreId(storeId).stream()
                .map(menu -> ChatStoreMenuResponse.builder()
                        .menuId(menu.getMenuId())
                        .name(menu.getName())
                        .price(menu.getPrice())
                        .salesStatus(menu.getSalesStatus() == null ? null : menu.getSalesStatus().name())
                        .imageId(getFirstImageId("MENU", menu.getMenuId()))
                        .build())
                .toList();

        List<StoreReview> activeReviews = chatStoreReviewRepository
                .findByStore_StoreIdAndStatusOrderByCreatedAtDesc(storeId, ReviewStatus.ACTIVE);

        List<ChatStoreReviewResponse> reviews = activeReviews.stream()
                .map(review -> ChatStoreReviewResponse.builder()
                        .reviewId(review.getReviewId())
                        .rating(review.getRating())
                        .content(review.getContent())
                        .writerNickname(getNickname(review.getMember()))
                        .createdAt(review.getCreatedAt())
                        .ownerReply(review.getOwnerReply())
                        .imageIds(getReviewImageIds(review.getReviewId()))
                        .build())
                .toList();

        return ChatStoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .address(store.getAddress())
                .category(store.getCategory())
                .phone(store.getPhone())
                .operatingHours(store.getOperatingHours())
                .businessStatus(store.getBusinessStatus() == null ? null : store.getBusinessStatus().name())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .imageId(getFirstImageId("STORE", store.getStoreId()))
                .averageRating(getAverageRating(activeReviews))
                .reviewCount(activeReviews.size())
                .menus(menus)
                .reviews(reviews)
                .build();
    }

    // 채팅방 종료 후 예약자 식당 리뷰 대상 조회
    public ChatStoreReviewTargetResponse getStoreReviewTarget(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        checkChatRoomMember(chatRoom, loginMember);

        List<ChatForm> reservationForms =
                chatFormRepository.findAllByChatRoomAndReservationIdIsNotNullOrderByUpdatedAtDesc(chatRoom);

        for (ChatForm chatForm : reservationForms) {
            Reservation reservation = reservationRepository.findById(chatForm.getReservationId())
                    .orElse(null);

            if (reservation == null) {
                continue;
            }

            if (!reservation.getMember().getMemberId().equals(loginMember.getMemberId())) {
                continue;
            }

            boolean alreadyReviewed = storeReviewRepository.existsByReservation(reservation);
            boolean completed = reservation.getStatus() == ReservationStatus.COMPLETED;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime allowedStartTime = reservation.getReservationAt();
            LocalDateTime allowedEndTime = reservation.getReservationAt().plusDays(3);

            boolean withinReviewPeriod =
                    !now.isBefore(allowedStartTime)
                            && !now.isAfter(allowedEndTime);

            boolean canReview =
                    completed
                            && !alreadyReviewed
                            && withinReviewPeriod;

            return ChatStoreReviewTargetResponse.builder()
                    .exists(true)
                    .reservationId(reservation.getReservationId())
                    .storeId(reservation.getStore().getStoreId())
                    .storeName(reservation.getStore().getName())
                    .reservationAt(reservation.getReservationAt())
                    .reservationStatus(reservation.getStatus().name())
                    .alreadyReviewed(alreadyReviewed)
                    .canReview(canReview)
                    .guideMessage(getStoreReviewGuideMessage(reservation, alreadyReviewed, withinReviewPeriod))
                    .build();
        }

        return ChatStoreReviewTargetResponse.builder()
                .exists(false)
                .canReview(false)
                .alreadyReviewed(false)
                .guideMessage("작성할 식당 리뷰가 없습니다.")
                .build();
    }

    private String getStoreReviewGuideMessage(
            Reservation reservation,
            boolean alreadyReviewed,
            boolean withinReviewPeriod
    ) {
        if (alreadyReviewed) {
            return "이미 식당 리뷰를 작성했습니다.";
        }

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            return "점주가 예약을 완료 처리한 뒤 식당 리뷰를 작성할 수 있습니다.";
        }

        if (!withinReviewPeriod) {
            return "식당 리뷰는 예약 시간부터 3일 안에 작성할 수 있습니다.";
        }

        return "식당 리뷰를 작성할 수 있습니다.";
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

        ChatFormOptionType optionType = getRequestOptionType(request);
        String optionText = request.getOptionText();
        ChatPlaceSource placeSource = request.getPlaceSource();
        Long storeId = request.getStoreId();
        String placeName = request.getPlaceName();
        String address = request.getAddress();
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        String mapUrl = request.getMapUrl();
        LocalDateTime appointmentAt = request.getAppointmentAt();
        String memo = getBlankToNull(request.getMemo());

        if (chatForm.isVoteForm()) {
            optionType = ChatFormOptionType.VOTE;
            optionText = getRequiredText(optionText);
            placeSource = null;
            storeId = null;
            placeName = null;
            address = null;
            latitude = null;
            longitude = null;
            mapUrl = null;
            appointmentAt = null;
            memo = null;
        } else if (optionType == ChatFormOptionType.TIME) {
            if (appointmentAt == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }

            optionText = getBlankToDefault(optionText, formatAppointmentOptionText(appointmentAt));
            placeSource = null;
            storeId = null;
            placeName = null;
            address = null;
            latitude = null;
            longitude = null;
            mapUrl = null;
        } else {
            optionType = ChatFormOptionType.PLACE;
            appointmentAt = null;
            memo = null;

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
                storeId = null;
                placeName = getRequiredText(placeName);
                optionText = getBlankToDefault(optionText, placeName);
            }
        }

        return new ChatFormOption(
                chatForm,
                loginMember,
                optionType,
                optionText,
                optionOrder,
                placeSource,
                storeId,
                placeName,
                address,
                latitude,
                longitude,
                mapUrl,
                appointmentAt,
                memo
        );
    }

    private ChatFormOptionResponse createOptionResponse(
            ChatFormOption option,
            Map<ChatFormOptionType, Long> mySelectedOptionIds
    ) {
        ChatFormOptionType optionType = getOptionType(option, option.getChatForm());

        return ChatFormOptionResponse.builder()
                .optionId(option.getChatFormOptionId())
                .optionType(optionType.name())
                .optionText(option.getOptionText())
                .optionOrder(option.getOptionOrder())
                .placeSource(option.getPlaceSource() == null ? null : option.getPlaceSource().name())
                .storeId(option.getStoreId())
                .placeName(option.getPlaceName())
                .address(option.getAddress())
                .latitude(option.getLatitude())
                .longitude(option.getLongitude())
                .mapUrl(option.getMapUrl())
                .appointmentAt(option.getAppointmentAt())
                .memo(option.getMemo())
                .createdByMemberId(option.getCreatedByMember().getMemberId())
                .createdByNickname(getNickname(option.getCreatedByMember()))
                .selectedByMe(option.getChatFormOptionId().equals(mySelectedOptionIds.get(optionType)))
                .build();
    }

    private Map<ChatFormOptionType, Long> getMySelectedOptionIds(ChatForm chatForm, Member loginMember) {
        Map<ChatFormOptionType, Long> selectedOptionIds = new HashMap<>();

        List<ChatFormAnswer> answers = chatFormAnswerRepository.findAllByChatFormAndMember(chatForm, loginMember);

        for (ChatFormAnswer answer : answers) {
            ChatFormOptionType answerType = answer.getAnswerType();

            if (answerType == null) {
                answerType = getOptionType(answer.getChatFormOption(), chatForm);
            }

            selectedOptionIds.put(answerType, answer.getChatFormOption().getChatFormOptionId());
        }

        return selectedOptionIds;
    }

    private boolean canCloseForm(ChatForm chatForm, Member loginMember) {
        if (chatForm == null || loginMember == null) {
            return false;
        }

        if (chatForm.isClosed()) {
            return false;
        }

        if (chatForm.getCreator() == null || chatForm.getCreator().getMemberId() == null) {
            return false;
        }

        return chatForm.getCreator().getMemberId().equals(loginMember.getMemberId());
    }

    private void confirmAppointmentByTimeVoteResult(ChatForm chatForm) {
        ChatFormOption timeWinner = getWinningOption(chatForm, ChatFormOptionType.TIME);

        if (timeWinner == null) {
            return;
        }

        if (timeWinner.getAppointmentAt() == null) {
            return;
        }

        LocalDateTime appointmentAt = timeWinner.getAppointmentAt();
        LocalDateTime autoCloseAt = appointmentAt.plusHours(CHAT_ROOM_AUTO_CLOSE_AFTER_APPOINTMENT_HOURS);

        chatForm.getChatRoom().confirmAppointment(appointmentAt, autoCloseAt);
    }

    private void createReservationByVoteResult(ChatForm chatForm, Member loginMember) {
        ChatFormOption placeWinner = getWinningOption(chatForm, ChatFormOptionType.PLACE);
        ChatFormOption timeWinner = getWinningOption(chatForm, ChatFormOptionType.TIME);

        if (placeWinner == null || timeWinner == null) {
            return;
        }

        if (timeWinner.getAppointmentAt() == null) {
            return;
        }

        if (placeWinner.getPlaceSource() != ChatPlaceSource.STORE) {
            return;
        }

        if (placeWinner.getStoreId() == null) {
            return;
        }

        int peopleCount = chatRoomMemberRepository
                .findAllByChatRoom(chatForm.getChatRoom())
                .size();

        ReservationRequest request = new ReservationRequest();
        request.setStoreId(placeWinner.getStoreId());
        request.setReservationAt(timeWinner.getAppointmentAt());
        request.setPeopleCount(peopleCount);
        request.setRequestMemo(
                "[채팅 약속 투표 예약] 채팅방 ID: "
                        + chatForm.getChatRoom().getChatRoomId()
                        + ", 폼 ID: "
                        + chatForm.getChatFormId()
        );

        Long reservationId = reservationService.createReservation(loginMember.getLoginId(), request);
        chatForm.connectReservation(reservationId);
    }

    private ChatFormOption getWinningOption(ChatForm chatForm, ChatFormOptionType optionType) {
        List<ChatFormOption> options = chatFormOptionRepository.findAllByChatFormOrderByOptionOrderAsc(chatForm).stream()
                .filter(option -> getOptionType(option, chatForm) == optionType)
                .toList();

        if (options.isEmpty()) {
            return null;
        }

        ChatFormOption winner = null;
        int winnerVoteCount = -1;

        for (ChatFormOption option : options) {
            int voteCount = chatFormAnswerRepository.countByChatFormOption(option);

            if (winner == null || voteCount > winnerVoteCount) {
                winner = option;
                winnerVoteCount = voteCount;
            }
        }

        if (winnerVoteCount <= 0) {
            return null;
        }

        return winner;
    }

    private ChatFormType normalizeFormType(ChatFormType formType) {
        if (formType == null) {
            return ChatFormType.VOTE;
        }

        if (formType == ChatFormType.VOTE) {
            return ChatFormType.VOTE;
        }

        return ChatFormType.PLACE;
    }

    private ChatFormOptionType getRequestOptionType(ChatFormOptionRequest request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getOptionType() != null) {
            return request.getOptionType();
        }

        if (request.getAppointmentAt() != null) {
            return ChatFormOptionType.TIME;
        }

        return ChatFormOptionType.PLACE;
    }

    private ChatFormOptionType getOptionType(ChatFormOption option, ChatForm chatForm) {
        if (option.getOptionType() != null) {
            return option.getOptionType();
        }

        if (chatForm != null && chatForm.isVoteForm()) {
            return ChatFormOptionType.VOTE;
        }

        if (option.getAppointmentAt() != null) {
            return ChatFormOptionType.TIME;
        }

        return ChatFormOptionType.PLACE;
    }

    private String getFormCardMessage(ChatForm chatForm) {
        if (chatForm.getFormType() == ChatFormType.PLACE) {
            return "[약속 투표] " + chatForm.getTitle();
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
        if (member == null) {
            return "회원";
        }

        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }

    private Long getFirstImageId(String targetType, Long targetId) {
        return fileAttachmentRepository.findByTargetTypeAndTargetId(targetType, targetId).stream()
                .map(FileAttachment::getFileId)
                .findFirst()
                .orElse(null);
    }

    private List<Long> getReviewImageIds(Long reviewId) {
        return fileAttachmentRepository.findByTargetTypeAndTargetId("REVIEW", reviewId).stream()
                .map(FileAttachment::getFileId)
                .toList();
    }

    private Double getAverageRating(List<StoreReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double average = reviews.stream()
                .filter(review -> review.getRating() != null)
                .mapToInt(StoreReview::getRating)
                .average()
                .orElse(0.0);

        return Math.round(average * 10.0) / 10.0;
    }

    private String formatAppointmentOptionText(LocalDateTime appointmentAt) {
        return appointmentAt.getMonthValue() + "월 "
                + appointmentAt.getDayOfMonth() + "일 "
                + String.format("%02d:%02d", appointmentAt.getHour(), appointmentAt.getMinute());
    }

    private String getRequiredText(String text) {
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

    private String getBlankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
