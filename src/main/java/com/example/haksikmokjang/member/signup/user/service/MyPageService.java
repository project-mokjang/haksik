package com.example.haksikmokjang.member.signup.user.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.dto.MyPageResponse;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserProfileRepository userProfileRepository;

    public MyPageResponse getMyPageInfo(Member member) {
        // Member와 연결된 UserProfile 정보 꺼내오기
        UserProfile profile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return MyPageResponse.builder()
                .memberId(member.getMemberId())
                .name(profile.getName())
                .nickname(profile.getNickname())
                .schoolName(profile.getSchool().getSchoolName())
                .department(profile.getDepartment())
                .loginId(member.getLoginId())
                .profileImageUrl(profile.getProfileImage() != null ? profile.getProfileImage().getStoredPath() : null)
                .preferredFoodCategory(profile.getPreferredFoodCategory())
                .build();
    }
}