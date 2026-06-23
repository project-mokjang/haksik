package com.example.haksikmokjang.member.signup.user.service;

import com.example.haksikmokjang.fileattachment.service.FileAttachmentService;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.member.signup.user.dto.ProfileUpdateRequest;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final SchoolRepository schoolRepository;

    private final FileAttachmentRepository fileAttachmentRepository;
    private final FileAttachmentService fileAttachmentService;

    @Transactional
    public void updateProfile(Member member, ProfileUpdateRequest request) {

        //  내 프로필 찾기
        UserProfile profile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        //  닉네임 중복 검사
        if (!profile.getNickname().equals(request.getNickname()) &&
                userProfileRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }

        //  텍스트 정보 바꾸기
        profile.updateInfo(request.getNickname(), request.getDepartment(), request.getPreferredFoodCategory());

        //  DB에서 학교 찾기 (
        School newSchool = schoolRepository.findBySchoolName(request.getSchoolName())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOOL_NOT_FOUND));

        //  학교 업데이트
        userProfileRepository.updateSchoolByMember(member, newSchool);

        MultipartFile profileImageFile = request.getProfileImage();

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            Long fileId = fileAttachmentService.uploadFile(
                    member.getLoginId(),
                    profileImageFile,
                    "USER_PROFILE",
                    member.getMemberId()
            );

            FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

            userProfileRepository.updateProfileImageByMember(member, fileAttachment);
        }
}
    @Transactional
    public void updatePreferredFood(Member member, String foodCategory) {
        // 내 프로필 찾기
        UserProfile profile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 음식 카테고리만  업데이트
        profile.updatePreferredFood(foodCategory);
    }
    }