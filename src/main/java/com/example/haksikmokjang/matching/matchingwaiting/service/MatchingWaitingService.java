package com.example.haksikmokjang.matching.matchingwaiting.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.dto.LocationUpdateRequest;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingEnterRequest;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingMarkerResponse;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.core.repository.MemberLocationRepository;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        public void enterWaiting(Long memberId, MatchingWaitingEnterRequest request) {

            System.out.println("서비스 진입 memberId = " + memberId); //테스트 코드

            // 로그인한 회원 ID로 일반 사용자 프로필을 조회
            UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

            System.out.println("userProfileId = " + userProfile.getUserProfileId()); // 테스트 코드

            // 기존 위치 정보가 있으면 가져오고, 없으면 새 위치 객체를 생성
            MemberLocation location = memberLocationRepository.findByUserProfile(userProfile)
                    .orElseGet(() -> MemberLocation.builder()
                            .userProfile(userProfile)
                            .locationPublicYn("Y")
                            .build());

            // 브라우저에서 전달받은 현재 위치 값으로 사용자 위치를 갱신
            location.updateLocation(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getAccuracyRangeM()
            );

            // 위치 정보는 회원당 1개 row만 유지하므로 있으면 update, 없으면 insert
            memberLocationRepository.save(location);

            // 이미 대기 중인 매칭이 있으면 중복 대기를 막기 위해 기존 대기 상태를 취소 처리
            matchingWaitingRepository.findByUserProfileAndStatus(
                            userProfile,
                            MatchingWaitingStatus.WAITING
                    )
                    .ifPresent(MatchingWaiting::cancel);

            // 새 매칭 대기 정보를 생성한다. 대기 시간은 현재 시점부터 1시간으로 설정
            MatchingWaiting waiting = MatchingWaiting.builder()
                    .userProfile(userProfile)
                    .mode(request.getMode())
                    .status(MatchingWaitingStatus.WAITING)
                    .message(request.getMessage())
                    .expiredAt(LocalDateTime.now().plusHours(1))
                    .build();

            // 새 매칭 대기 상태를 저장
            matchingWaitingRepository.save(waiting);
        }

    @Transactional(readOnly = true)
    public List<MatchingWaitingMarkerResponse> getMarkers(MatchingMode mode, Long memberId) {

        // 로그인한 회원의 UserProfile을 조회
        UserProfile loginUserProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 선택한 매칭 모드에서 WAITING 상태이고, 아직 만료되지 않은 대기 목록만 조회
        List<MatchingWaiting> waitings =
                matchingWaitingRepository.findByModeAndStatusAndExpiredAtAfter(
                        mode,
                        MatchingWaitingStatus.WAITING,
                        LocalDateTime.now()
                );

        // 대기 목록을 지도 마커 응답 DTO로 변환한
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

                    return new MatchingWaitingMarkerResponse(
                            waiting.getWaitingId(),
                            userProfile.getUserProfileId(),
                            userProfile.getNickname(),
                            waiting.getMode(),
                            waiting.getMessage(),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAccuracyRangeM(),
                            mine
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void cancelWaiting(Long memberId) {

        // 로그인한 회원 ID로 일반 사용자 프로필을 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 현재 WAITING 상태인 매칭 대기 정보를 조회
        MatchingWaiting waiting = matchingWaitingRepository.findByUserProfileAndStatus(
                        userProfile,
                        MatchingWaitingStatus.WAITING
                )
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_WAITING_NOT_FOUND));

        // 매칭 대기 상태를 CANCELED로 변경
        waiting.cancel();
    }

    public void expireOldWaitings() {

        // 만료된 WAITING 목록 조회
        List<MatchingWaiting> expiredWaitings =
                matchingWaitingRepository.findByStatusAndExpiredAtBefore(
                        MatchingWaitingStatus.WAITING,
                        LocalDateTime.now()
                );

        // EXPIRED 상태로 변경
        expiredWaitings.forEach(MatchingWaiting::expire);
    }

    public void updateLocation(Long memberId, LocationUpdateRequest request) {

        // 사용자 프로필 조회
        UserProfile userProfile = userProfileRepository.findByMember_MemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 위치 정보 조회
        MemberLocation location = memberLocationRepository.findByUserProfile(userProfile)
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));

        // 현재 위치 갱신
        location.updateLocation(
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracyRangeM()
        );
    }
}
