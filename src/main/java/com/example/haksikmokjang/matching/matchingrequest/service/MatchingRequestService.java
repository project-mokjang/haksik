package com.example.haksikmokjang.matching.matchingrequest.service;

import com.example.haksikmokjang.chat.service.ChatService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingrequest.domain.Matching;
import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingReceivedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingRequestCreateRequest;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingSentResponse;
import com.example.haksikmokjang.matching.matchingrequest.repository.MatchingRepository;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchingRequestService {

    private final MatchingRepository matchingRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;
    private final UserProfileRepository userProfileRepository;
    private final ChatService chatService;

    // 매칭 요청 생성
    public void requestMatching(Long memberId, MatchingRequestCreateRequest request) {

        // 신청자 프로필 조회
        UserProfile requester = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

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

        // 동일 사용자 간 진행 중인 요청 방지
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

        matchingRepository.save(matching);
    }

    // 받은 매칭 요청 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingReceivedResponse> getReceivedRequests(Long memberId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 내가 받은 요청 조회
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

    // 보낸 매칭 요청 목록 조회
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

    // 보낸 매칭 요청 취소
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
    }

    // 매칭 요청 수락
    public void acceptMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 요청 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 요청 수신자 확인
        if (!matching.getTarget().getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_NOT_RECEIVER);
        }

        // 요청 상태 확인
        if (matching.getStatus() != MatchingStatus.REQUESTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 수락 처리
        matching.accept();

        if (matching.getMatchingType() == MatchingType.GROUP_MEAL) {

            // 단체 학식 매칭 처리
            acceptGroupMealMatching(matching);

            // TODO 단체 학식 채팅방 연결
            // chatService.addGroupMealParticipant(...);

        } else {

            // 1:1 매칭 처리
            acceptOneToOneMatching(matching);

            // 1:1 채팅방 생성
            Long requesterMemberId = matching.getRequester().getMember().getMemberId();
            Long targetMemberId = matching.getTarget().getMember().getMemberId();

            chatService.createMealChatRoom(requesterMemberId, targetMemberId);
        }
    }

    // 매칭 요청 거절
    public void rejectMatching(Long memberId, Long matchingId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 매칭 요청 조회
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        // 요청 수신자 확인
        if (!matching.getTarget().getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_NOT_RECEIVER);
        }

        // 요청 상태 확인
        if (matching.getStatus() != MatchingStatus.REQUESTED) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_PROCESSED);
        }

        // 거절 처리
        matching.reject();
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
}