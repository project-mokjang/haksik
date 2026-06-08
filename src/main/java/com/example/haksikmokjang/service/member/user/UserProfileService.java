package com.example.haksikmokjang.service.member.user;

import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.dto.member.user.ProfileUpdateRequest;
import com.example.haksikmokjang.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;


    @Transactional
    public void updateProfile(Member member, ProfileUpdateRequest request) {

        // 1. 내 프로필 찾기
        UserProfile profile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        // 2. 닉네임 중복 검사
        if (!profile.getNickname().equals(request.getNickname()) &&
                userProfileRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3. 텍스트 정보 바꾸기
        profile.updateInfo(request.getNickname(), request.getDepartment(), request.getPreferredFoodCategory());

        // 4. 사진 바꾸기: 만약 프론트에서 새 사진을 보냈다면?
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {

        }
    }
}
