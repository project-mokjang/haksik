package com.example.haksikmokjang.matching.matchingwaiting.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.dto.LocationUpdateRequest;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingEnterRequest;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingMarkerResponse;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.core.repository.MemberLocationRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchingWaitingService {

    private final UserProfileRepository userProfileRepository;
    private final MemberLocationRepository memberLocationRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;

    // 매칭 대기 입장
    public void enterWaiting(Long memberId, MatchingWaitingEnterRequest request) {

        // 로그인한 회원의 사용자 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 기존 WAITING 상태가 있으면 새 대기 생성 차단
        matchingWaitingRepository.findByUserProfileAndStatus(
                userProfile,
                MatchingWaitingStatus.WAITING
        ).ifPresent(waiting -> {
            if (waiting.getExpiredAt().isAfter(LocalDateTime.now())) {
                throw new CustomException(ErrorCode.MATCHING_WAITING_ALREADY_EXISTS);
            }

            waiting.expire();
        });

        // 기존 위치 정보 조회 또는 생성
        MemberLocation location = memberLocationRepository.findByUserProfile(userProfile)
                .orElseGet(() -> MemberLocation.builder()
                        .userProfile(userProfile)
                        .locationPublicYn("Y")
                        .build());

        // 현재 위치 갱신
        location.updateLocation(
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracyRangeM()
        );

        memberLocationRepository.save(location);

        // 매칭 유형 결정
        MatchingType matchingType = resolveMatchingType(request);

        // 최대 참여 인원 결정
        Integer maxParticipants = resolveMaxParticipants(request, matchingType);

        // 새 매칭 대기 생성
        MatchingWaiting waiting = MatchingWaiting.builder()
                .userProfile(userProfile)
                .mode(request.getMode())
                .matchingType(matchingType)
                .maxParticipants(maxParticipants)
                .currentParticipants(1)
                .status(MatchingWaitingStatus.WAITING)
                .message(request.getMessage())
                .expiredAt(LocalDateTime.now().plusHours(1))
                .build();

        matchingWaitingRepository.save(waiting);
    }

    // 지도 마커 조회
    @Transactional(readOnly = true)
    public List<MatchingWaitingMarkerResponse> getMarkers(MatchingMode mode, Long memberId) {

        // 로그인한 회원의 사용자 프로필 조회
        UserProfile loginUserProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 선택 모드의 대기 목록 조회
        List<MatchingWaiting> waitings = matchingWaitingRepository.findByModeAndStatusAndExpiredAtAfter(
                mode,
                MatchingWaitingStatus.WAITING,
                LocalDateTime.now()
        );

        // 지도 마커 응답 변환
        return waitings.stream()
                .map(waiting -> {
                    UserProfile userProfile = waiting.getUserProfile();

                    MemberLocation location = memberLocationRepository.findByUserProfile(userProfile)
                            .orElse(null);

                    if (location == null) {
                        return null;
                    }

                    boolean mine = userProfile.getUserProfileId()
                            .equals(loginUserProfile.getUserProfileId());

                    return MatchingWaitingMarkerResponse.from(waiting, location, mine);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 매칭 대기 취소
    public void cancelWaiting(Long memberId) {

        // 로그인한 회원의 사용자 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 현재 대기 중인 매칭 조회
        MatchingWaiting waiting = matchingWaitingRepository.findByUserProfileAndStatus(
                        userProfile,
                        MatchingWaitingStatus.WAITING
                )
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_WAITING_NOT_FOUND));

        // 대기 취소 처리
        waiting.cancel();
    }

    // 만료된 대기 처리
    public void expireOldWaitings() {

        // 만료된 대기 목록 조회
        List<MatchingWaiting> expiredWaitings = matchingWaitingRepository.findByStatusAndExpiredAtBefore(
                MatchingWaitingStatus.WAITING,
                LocalDateTime.now()
        );

        // 만료 상태 변경
        expiredWaitings.forEach(MatchingWaiting::expire);
    }

    // 현재 위치 갱신
    public void updateLocation(Long memberId, LocationUpdateRequest request) {

        // 로그인한 회원의 사용자 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 위치 정보 조회 또는 생성
        MemberLocation location = memberLocationRepository.findByUserProfile(userProfile)
                .orElseGet(() -> MemberLocation.builder()
                        .userProfile(userProfile)
                        .locationPublicYn("Y")
                        .build());

        // 현재 위치 갱신
        location.updateLocation(
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracyRangeM()
        );

        memberLocationRepository.save(location);
    }

    // 요청값 기준으로 매칭 유형 결정
    private MatchingType resolveMatchingType(MatchingWaitingEnterRequest request) {

        if (request.getMode() == MatchingMode.BLIND_DATE) {
            return MatchingType.ONE_TO_ONE;
        }

        if (request.getMode() == MatchingMode.GROUP) {
            return MatchingType.GROUP_DATE;
        }

        if (request.getMatchingType() == null) {
            return MatchingType.ONE_TO_ONE;
        }

        if (request.getMatchingType() == MatchingType.GROUP_DATE) {
            throw new CustomException(ErrorCode.INVALID_MATCHING_MODE);
        }

        return request.getMatchingType();
    }

    // 매칭 유형 기준으로 최대 참여 인원 결정
    private Integer resolveMaxParticipants(
            MatchingWaitingEnterRequest request,
            MatchingType matchingType
    ) {
        if (matchingType == MatchingType.ONE_TO_ONE) {
            return 2;
        }

        if (matchingType == MatchingType.GROUP_DATE) {
            return 2;
        }

        Integer maxParticipants = request.getMaxParticipants();

        if (maxParticipants == null || maxParticipants < 3) {
            throw new CustomException(ErrorCode.INVALID_MATCHING_PARTICIPANTS);
        }

        return maxParticipants;
    }
}