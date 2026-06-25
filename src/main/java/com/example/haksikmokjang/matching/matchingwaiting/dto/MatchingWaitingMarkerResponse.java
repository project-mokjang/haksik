package com.example.haksikmokjang.matching.matchingwaiting.dto;

import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Getter
@AllArgsConstructor
// 마커 응답 DTO, 지도에 매칭 대기 중인 사용자를 띄우고, 클릭시 간단한 사용자 프로필 정보까지 전달
public class MatchingWaitingMarkerResponse {

    private Long waitingId;

    private Long userProfileId;

    private String nickname;

    private MatchingMode mode;

    private MatchingType matchingType;

    private String message;

    private Integer currentParticipants;

    private Integer maxParticipants;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer accuracyRangeM;

    private boolean mine; // 내 마커인지 구분

    private String profileImageUrl;

    private String gender;

    private Integer age;

    private String department;

    private String preferredFoodCategory;

    private BigDecimal mannerTemperature;

    // 지도 마커 응답 생성
    public static MatchingWaitingMarkerResponse from(
            MatchingWaiting waiting,
            MemberLocation location,
            boolean mine
    ) {
        UserProfile userProfile = waiting.getUserProfile();

        return new MatchingWaitingMarkerResponse(
                waiting.getWaitingId(),
                userProfile.getUserProfileId(),
                userProfile.getNickname(),
                waiting.getMode(),
                waiting.getMatchingType(),
                waiting.getMessage(),
                waiting.getCurrentParticipants(),
                waiting.getMaxParticipants(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracyRangeM(),
                mine,
                getProfileImageUrl(userProfile),
                userProfile.getGender() == null ? null : String.valueOf(userProfile.getGender()),
                getAge(userProfile),
                userProfile.getDepartment(),
                userProfile.getPreferredFoodCategory() == null
                        ? null
                        : String.valueOf(userProfile.getPreferredFoodCategory()),
                userProfile.getMannerTemperature()
        );
    }

    private static String getProfileImageUrl(UserProfile userProfile) {
        if (userProfile.getProfileImage() == null) {
            return null;
        }

        return "/api/images/" + userProfile.getProfileImage().getFileId();
    }

    private static Integer getAge(UserProfile userProfile) {
        if (userProfile.getBirthDate() == null) {
            return null;
        }

        return Period.between(userProfile.getBirthDate(), LocalDate.now()).getYears();
    }
}
