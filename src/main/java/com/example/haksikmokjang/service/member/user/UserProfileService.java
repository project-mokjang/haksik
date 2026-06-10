package com.example.haksikmokjang.service.member.user;

import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.fileattachment.FileAttachment;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.domain.school.School;
import com.example.haksikmokjang.dto.member.user.ProfileUpdateRequest;
import com.example.haksikmokjang.repository.FileAttachmentRepository;
import com.example.haksikmokjang.repository.UserProfileRepository;
import com.example.haksikmokjang.repository.SchoolRepository;
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
            try {
                // 1) 사진을 저장할 진짜 폴더 경로 만들기
                String saveDirPath = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
                File dir = new File(saveDirPath);
                if (!dir.exists()) dir.mkdirs();

                // 2) 랜덤 이름으로 파일명 바꾸기 (중복 방지)
                String originalName = profileImageFile.getOriginalFilename();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String savedName = UUID.randomUUID().toString() + extension;

                // 3) 폴더에 사진 찰칵! 저장하기
                profileImageFile.transferTo(new File(saveDirPath + savedName));

                // 4) DB에 넣을 파일 정보 예쁘게 포장하기
                FileAttachment fileAttachment = FileAttachment.builder()
                        .uploader(member)
                        .targetType("USER_PROFILE")
                        .originalName(originalName)
                        .storedPath("/uploads/" + savedName)
                        .extension(extension)
                        .fileSize(profileImageFile.getSize())
                        .build();

                //  파일 창고에 정보 저장하고, 프로필이랑 연결하기!
                fileAttachmentRepository.save(fileAttachment);
                userProfileRepository.updateProfileImageByMember(member, fileAttachment);

            } catch (Exception e) {
                // 저장하다가 문제 생기면 서버 에러 띄우기
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
    }
}
    }