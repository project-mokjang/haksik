package com.example.haksikmokjang.service.member.user;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.dto.member.user.MyPageResponse;
import com.example.haksikmokjang.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserProfileRepository userProfileRepository;

    public MyPageResponse getMyPageInfo(Member member) {
        // 1. Member와 연결된 UserProfile 정보 꺼내오기
        UserProfile profile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return MyPageResponse.builder()
                .name(profile.getName())

                .nickname(profile.getNickname())
                .schoolName(profile.getSchool().getSchoolName())
                .department(profile.getDepartment())
                .loginId(member.getLoginId())
                .profileImageUrl(profile.getProfileImage() != null ? profile.getProfileImage().getStoredPath() : null)
                .build();
    }
}