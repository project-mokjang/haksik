package com.example.haksikmokjang.matching.matchingrequest.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingrequest.domain.Matching;
import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingReceivedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingRequestCreateRequest;
import com.example.haksikmokjang.matching.matchingrequest.repository.MatchingRepository;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchingRequestService {

    private final MatchingRepository matchingRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;
    private final UserProfileRepository userProfileRepository;

    public void requestMatching(Long memberId, MatchingRequestCreateRequest request) {

        // 신청자 프로필 조회
        UserProfile requester = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 상대 대기 정보 조회
        MatchingWaiting targetWaiting = matchingWaitingRepository.findById(request.getTargetWaitingId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_TARGET_NOT_FOUND));

        // 상대 대기 상태 확인
        if (targetWaiting.getStatus() != MatchingWaitingStatus.WAITING) {
            throw new CustomException(ErrorCode.MATCHING_TARGET_NOT_WAITING);
        }

        // 상대 프로필 조회
        UserProfile target = targetWaiting.getUserProfile();

        // 자기 자신 신청 방지
        if (requester.getUserProfileId().equals(target.getUserProfileId())) {
            throw new CustomException(ErrorCode.MATCHING_SELF_REQUEST);
        }

        // 중복 요청 방지
        boolean alreadyRequested = matchingRepository.existsRequestBetween(
                requester,
                target,
                MatchingStatus.REQUESTED
        );

        if (alreadyRequested) {
            throw new CustomException(ErrorCode.MATCHING_ALREADY_REQUESTED);
        }

        // 매칭 요청 생성
        Matching matching = Matching.builder()
                .mode(targetWaiting.getMode())
                .requester(requester)
                .target(target)
                .status(MatchingStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        // 매칭 요청 저장
        matchingRepository.save(matching);
    }

    // 내가 받은 매칭 요청 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingReceivedResponse> getReceivedRequests(Long memberId) {

        // 내 프로필 조회
        UserProfile target = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 내가 받은 REQUESTED 상태 요청 조회
        List<Matching> matchings = matchingRepository.findByTargetAndStatusOrderByRequestedAtDesc(
                target,
                MatchingStatus.REQUESTED
        );

        // 응답 DTO 변환
        return matchings.stream()
                .map(matching -> new MatchingReceivedResponse(
                        matching.getMatchingId(),
                        matching.getRequester().getUserProfileId(),
                        matching.getRequester().getNickname(),
                        matching.getMode(),
                        matching.getStatus(),
                        matching.getRequestedAt()
                ))
                .toList();
    }

    // 매칭 요청 수락
    public void acceptMatching(Long memberId, Long matchingId) {

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

        // 신청자 대기 상태 종료
        matchingWaitingRepository.findByUserProfileAndStatus(
                matching.getRequester(),
                MatchingWaitingStatus.WAITING
        ).ifPresent(MatchingWaiting::matched);

        // 수신자 대기 상태 종료
        matchingWaitingRepository.findByUserProfileAndStatus(
                matching.getTarget(),
                MatchingWaitingStatus.WAITING
        ).ifPresent(MatchingWaiting::matched);
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
    }
}