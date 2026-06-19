package com.example.haksikmokjang.matching.matchingrequest.service;

import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingrequest.domain.Matching;
import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingAcceptedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingReceivedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingRequestCreateRequest;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingSentResponse;
import com.example.haksikmokjang.matching.matchingrequest.repository.MatchingRepository;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchingRequestService {

    private final MatchingRepository matchingRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;
    private final ChatRoomCreateService chatRoomCreateService;

    // 매칭 요청 생성
    public void requestMatching(Long memberId, MatchingRequestCreateRequest request) {

        // 신청자 프로필 조회
        UserProfile requester = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 확정된 매칭이 있으면 새 매칭 신청 차단
        boolean hasAcceptedMatching = !matchingRepository
                .findByParticipantAndStatusOrderByRespondedAtDesc(
                        requester,
                        MatchingStatus.ACCEPTED
                )
                .isEmpty();

        if (hasAcceptedMatching) {
            throw new CustomException(ErrorCode.MATCHING_ACCEPTED_ALREADY_EXISTS);
        }

        // 신청 대상 대기 정보 조회
        MatchingWaiting targetWaiting = matchingWaitingRepository.findById(request.getTargetWaitingId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_TARGET_NOT_FOUND));

        // 신청 대상 대기 상태 확인
        if (targetWaiting.getStatus() != MatchingWaitingStatus.WAITING) {
            throw new CustomException(ErrorCode.MATCHING_TARGET_NOT_WAITING);
        }

        // 단체 학식 모집 정원 확인
        if (targetWaiting.isGroupMeal() && targetWaiting.isFull()) {
            throw new CustomException(ErrorCode.MATCHING_PARTICIPANTS_FULL);
        }

        // 신청 대상 프로필 조회
        UserProfile target = targetWaiting.getUserProfile();

        // 자기 자신에게 신청 방지
        if (requester.getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_SELF_REQUEST);
        }

        // 단체방 모집자의 다른 매칭 신청 방지
        matchingWaitingRepository.findByUserProfileAndStatus(
                requester,
                MatchingWaitingStatus.WAITING
        ).ifPresent(myWaiting -> {
            if (myWaiting.getMatchingType() == MatchingType.GROUP_MEAL) {
                throw new CustomException(ErrorCode.MATCHING_GROUP_OWNER_CANNOT_REQUEST);
            }
        });

        // 같은 모집방 중복 신청/참여 방지
        boolean alreadyRequestedOrJoined =
                matchingRepository.existsByRequesterAndTargetWaitingAndStatusIn(
                        requester,
                        targetWaiting,
                        List.of(MatchingStatus.REQUESTED, MatchingStatus.ACCEPTED)
                );

        if (alreadyRequestedOrJoined) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PARTICIPATED);
        }

        // 동일 사용자 간 진행 중 요청 방지
        boolean alreadyRequested = matchingRepository.existsRequestBetween(
                requester,
                target,
                MatchingStatus.REQUESTED
        );

        if (alreadyRequested) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_REQUESTED);
        }

        // 매칭 요청 저장
        Matching matching = Matching.builder()
                .mode(targetWaiting.getMode())
                .matchingType(targetWaiting.getMatchingType())
                .requester(requester)
                .target(target)
                .targetWaiting(targetWaiting)
                .status(MatchingStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        Matching savedMatching = matchingRepository.save(matching);

        sendMatchingNotification(
                target,
                "매칭 신청",
                getNotificationNickname(requester) + "님이 매칭을 신청했습니다.",
                savedMatching.getMatchingId()
        );
    }

    // 확정된 매칭 취소
    @Transactional
    public void cancelAcceptedMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 매칭 참여자인지 확인
        boolean isRequester = matching.getRequester().getUserProfileId()
                .equals(userProfile.getUserProfileId());

        boolean isTarget = matching.getTarget().getUserProfileId()
                .equals(userProfile.getUserProfileId());

        if (!isRequester && !isTarget) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 확정된 매칭인지 확인
        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 단체 학식 모집자가 취소하는 경우
        if (matching.getMatchingType() == MatchingType.GROUP_MEAL && isTarget) {
            cancelGroupMealByOwner(matching.getTargetWaiting());
            return;
        }

        // 단체 학식 참가자가 취소하는 경우
        if (matching.getMatchingType() == MatchingType.GROUP_MEAL && isRequester) {
            MatchingWaiting targetWaiting = matching.getTargetWaiting();

            matching.cancel();
            targetWaiting.decreaseParticipant();

            matchingRepository.save(matching);
            matchingWaitingRepository.save(targetWaiting);

            sendMatchingNotification(
                    matching.getTarget(),
                    "단체 매칭 참가 취소",
                    getNotificationNickname(matching.getRequester()) + "님이 단체 매칭 참가를 취소했습니다.",
                    matching.getMatchingId()
            );

            return;
        }

        // 1:1 확정 매칭 취소
        UserProfile opponent = isRequester
                ? matching.getTarget()
                : matching.getRequester();

        matching.cancel();
        matchingRepository.save(matching);

        sendMatchingNotification(
                opponent,
                "매칭 취소",
                getNotificationNickname(userProfile) + "님이 만남을 취소했습니다.",
                matching.getMatchingId()
        );
    }

    // 단체방 모집자가 단체방 전체 취소
    private void cancelGroupMealByOwner(MatchingWaiting targetWaiting) {

        // 단체방 대기 상태 취소
        targetWaiting.cancel();

        // 해당 단체방에 연결된 신청/확정 매칭 전체 조회
        List<Matching> matchings = matchingRepository.findByTargetWaitingAndStatusIn(
                targetWaiting,
                List.of(MatchingStatus.REQUESTED, MatchingStatus.ACCEPTED)
        );

        // 연결된 요청 전체 취소 + 신청자/참가자에게 알림 전송
        matchings.forEach(matching -> {
            matching.cancel();

            sendMatchingNotification(
                    matching.getRequester(),
                    "단체방 취소",
                    getNotificationNickname(targetWaiting.getUserProfile()) + "님이 단체방을 취소했습니다.",
                    matching.getMatchingId()
            );
        });

        matchingRepository.saveAll(matchings);
        matchingWaitingRepository.save(targetWaiting);
    }

    // 내가 받은 매칭 요청 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingReceivedResponse> getReceivedRequests(Long memberId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 내가 받은 REQUESTED 요청 조회
        List<Matching> matchings = matchingRepository.findByTargetAndStatusOrderByRequestedAtDesc(
                target,
                MatchingStatus.REQUESTED
        );

        // 받은 요청 응답 변환
        return matchings.stream()
                .map(matching -> new MatchingReceivedResponse(
                        matching.getMatchingId(),
                        matching.getRequester().getUserProfileId(),
                        matching.getRequester().getNickname(),
                        matching.getMode(),
                        matching.getMatchingType(),
                        matching.getStatus(),
                        matching.getRequestedAt()
                ))
                .toList();
    }

    // 내가 보낸 매칭 요청 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingSentResponse> getSentRequests(Long memberId) {

        // 내 프로필 조회
        UserProfile requester = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 내가 보낸 REQUESTED 요청 조회
        List<Matching> matchings = matchingRepository.findByRequesterAndStatusOrderByRequestedAtDesc(
                requester,
                MatchingStatus.REQUESTED
        );

        // 보낸 요청 응답 변환
        return matchings.stream()
                .map(matching -> new MatchingSentResponse(
                        matching.getMatchingId(),
                        matching.getTarget().getUserProfileId(),
                        matching.getTarget().getNickname(),
                        matching.getTargetWaiting().getWaitingId(),
                        matching.getMode(),
                        matching.getMatchingType(),
                        matching.getStatus(),
                        matching.getTargetWaiting().getCurrentParticipants(),
                        matching.getTargetWaiting().getMaxParticipants(),
                        matching.getRequestedAt()
                ))
                .toList();
    }

    // 매칭 요청 수락
    public Long acceptMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 요청 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 요청 받은 사람인지 확인
        if (!matching.getTarget().getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_NOT_RECEIVER);
        }

        // REQUESTED 상태인지 확인
        if (matching.getStatus() != MatchingStatus.REQUESTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 수락 처리
        matching.accept();

        Long chatRoomId;

        if (matching.getMatchingType() == MatchingType.GROUP_MEAL) {

            // 단체 학식 매칭 처리
            acceptGroupMealMatching(matching);

            // 단체 학식은 정원이 다 찼을 때 3명 이상 채팅방 생성
            chatRoomId = createGroupMealChatRoomIfFull(matching);

        } else if (matching.getMatchingType() == MatchingType.GROUP_DATE) {

            // 과팅은 대표 2명 매칭 완료 처리
            acceptOneToOneMatching(matching);

            // 과팅은 리더 2명만 채팅방에 먼저 넣음
            chatRoomId = createGroupDateChatRoom(matching);

        } else {

            // 학식 1:1 / 소개팅 1:1 매칭 처리
            acceptOneToOneMatching(matching);

            // 모드에 따라 1:1 채팅방 생성
            chatRoomId = createOneToOneChatRoom(matching);
        }

        // 매칭 수락 알림
        sendMatchingNotification(
                matching.getRequester(),
                "매칭 수락",
                getNotificationNickname(matching.getTarget()) + "님이 매칭을 수락했습니다.",
                matching.getMatchingId()
        );

        return chatRoomId;
    }

    // 매칭 요청 거절
    public void rejectMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 요청 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 요청 받은 사람인지 확인
        if (!matching.getTarget().getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_NOT_RECEIVER);
        }

        // REQUESTED 상태인지 확인
        if (matching.getStatus() != MatchingStatus.REQUESTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 거절 처리
        matching.reject();

        // 거절 알림 처리
        sendMatchingNotification(
                matching.getRequester(),
                "매칭 거절",
                getNotificationNickname(matching.getTarget()) + "님이 매칭을 거절했습니다.",
                matching.getMatchingId()
        );
    }

    // 내가 보낸 매칭 요청 취소
    public void cancelSentMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile requester = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 요청 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 요청 보낸 사람인지 확인
        if (!matching.getRequester().getUserProfileId().equals(requester.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_NOT_RECEIVER);
        }

        // REQUESTED 상태인지 확인
        if (matching.getStatus() != MatchingStatus.REQUESTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 요청 취소 처리
        matching.cancel();

        // 매칭 취소 알림
        sendMatchingNotification(
                matching.getTarget(),
                "매칭 신청 취소",
                getNotificationNickname(matching.getRequester()) + "님이 매칭 신청을 취소했습니다.",
                matching.getMatchingId()
        );
    }

    // 1:1 매칭 수락 후 양쪽 대기 상태 종료
    private void acceptOneToOneMatching(Matching matching) {

        matchingWaitingRepository.findByUserProfileAndStatus(
                matching.getRequester(),
                MatchingWaitingStatus.WAITING
        ).ifPresent(MatchingWaiting::matched);

        matchingWaitingRepository.findByUserProfileAndStatus(
                matching.getTarget(),
                MatchingWaitingStatus.WAITING
        ).ifPresent(MatchingWaiting::matched);
    }

    // 단체 학식 수락 후 신청자 종료 및 모집 인원 증가
    private void acceptGroupMealMatching(Matching matching) {

        // 신청자 대기 상태 종료
        matchingWaitingRepository.findByUserProfileAndStatus(
                matching.getRequester(),
                MatchingWaitingStatus.WAITING
        ).ifPresent(MatchingWaiting::matched);

        // 신청 대상 모집방 조회
        MatchingWaiting targetWaiting = matching.getTargetWaiting();

        if (targetWaiting.getStatus() != MatchingWaitingStatus.WAITING) {
            throw new CustomException(ErrorCode.MATCHING_TARGET_NOT_WAITING);
        }

        if (targetWaiting.isFull()) {
            throw new CustomException(ErrorCode.MATCHING_PARTICIPANTS_FULL);
        }

        // 모집 인원 증가
        targetWaiting.increaseParticipant();
    }

    // 확정된 매칭 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingAcceptedResponse> getAcceptedRequests(Long memberId) {

        // 내 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 내가 참여 중인 ACCEPTED 매칭 조회
        List<Matching> matchings = matchingRepository.findByParticipantAndStatusOrderByRespondedAtDesc(
                userProfile,
                MatchingStatus.ACCEPTED
        );

        // 확정 매칭 응답 변환
        return matchings.stream()
                .map(matching -> {
                    UserProfile opponent = getOpponentProfile(matching, userProfile);

                    return new MatchingAcceptedResponse(
                            matching.getMatchingId(),
                            opponent.getUserProfileId(),
                            opponent.getNickname(),
                            matching.getTargetWaiting().getWaitingId(),
                            matching.getMode(),
                            matching.getMatchingType(),
                            matching.getStatus(),
                            matching.getTargetWaiting().getCurrentParticipants(),
                            matching.getTargetWaiting().getMaxParticipants(),
                            matching.getChatRoomId(),
                            matching.getRespondedAt()
                    );
                })
                .toList();
    }

    // 상대 프로필 조회
    private UserProfile getOpponentProfile(Matching matching, UserProfile userProfile) {

        if (matching.getRequester().getUserProfileId().equals(userProfile.getUserProfileId())) {
            return matching.getTarget();
        }

        return matching.getRequester();
    }

    // 매칭 알림 전송
    private void sendMatchingNotification(
            UserProfile receiver,
            String title,
            String content,
            Long matchingId
    ) {
        notificationService.sendNotification(
                receiver.getMember(),
                "MATCHING",
                title,
                content,
                "MATCHING",
                matchingId
        );
    }

    // 알림용 닉네임 조회
    private String getNotificationNickname(UserProfile userProfile) {
        if (userProfile.getNickname() == null || userProfile.getNickname().isBlank()) {
            return "상대방";
        }

        return userProfile.getNickname();
    }

    // 1:1 매칭 성공 후 채팅방 생성
    private Long createOneToOneChatRoom(Matching matching) {
        Long requesterMemberId = matching.getRequester().getMember().getMemberId();
        Long targetMemberId = matching.getTarget().getMember().getMemberId();

        ChatRoomResponse chatRoomResponse;

        if (matching.getMode() == MatchingMode.BLIND_DATE) {
            chatRoomResponse = chatRoomCreateService.createBlindDateChatRoom(
                    requesterMemberId,
                    targetMemberId
            );
        } else {
            chatRoomResponse = chatRoomCreateService.createMealChatRoom(
                    requesterMemberId,
                    targetMemberId
            );
        }

        matching.connectChatRoom(chatRoomResponse.getChatRoomId());

        return chatRoomResponse.getChatRoomId();
    }

    // 단체 학식 매칭 성공 후 정원이 다 찼으면 채팅방 생성
    private Long createGroupMealChatRoomIfFull(Matching matching) {
        MatchingWaiting targetWaiting = matching.getTargetWaiting();

        if (!targetWaiting.isFull()) {
            return null;
        }

        List<Matching> acceptedMatchings = matchingRepository.findByTargetWaitingAndStatusIn(
                targetWaiting,
                List.of(MatchingStatus.ACCEPTED)
        );

        List<Long> memberIds = new ArrayList<>();

        // 단체 학식 모집자
        memberIds.add(targetWaiting.getUserProfile().getMember().getMemberId());

        // 단체 학식 참가자들
        acceptedMatchings.forEach(acceptedMatching ->
                memberIds.add(acceptedMatching.getRequester().getMember().getMemberId())
        );

        ChatRoomResponse chatRoomResponse = chatRoomCreateService.createMealChatRoom(
                "학식메이트 채팅방",
                memberIds
        );

        acceptedMatchings.forEach(acceptedMatching ->
                acceptedMatching.connectChatRoom(chatRoomResponse.getChatRoomId())
        );

        matchingRepository.saveAll(acceptedMatchings);

        return chatRoomResponse.getChatRoomId();
    }

    // 과팅 매칭 성공 후 리더 2명만 채팅방 생성
    private Long createGroupDateChatRoom(Matching matching) {
        Long requesterMemberId = matching.getRequester().getMember().getMemberId();
        Long targetMemberId = matching.getTarget().getMember().getMemberId();

        ChatRoomResponse chatRoomResponse = chatRoomCreateService.createGroupDateChatRoom(
                "과팅 채팅방",
                List.of(requesterMemberId, targetMemberId)
        );

        matching.connectChatRoom(chatRoomResponse.getChatRoomId());

        return chatRoomResponse.getChatRoomId();
    }
}