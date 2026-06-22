package com.example.haksikmokjang.fileattachment.service;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final MemberRepository memberRepository;

    // 🚨 팩트: 하드디스크에 물리적으로 파일이 꽂힐 절대 경로
    private final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

    @Transactional
    public Long uploadFile(String loginId, MultipartFile file, String targetType, Long targetId) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        Member uploader = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        try {
            // 폴더 없으면 자동 생성 방어벽
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null) throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);

            // 확장자 분리 (".jpg") 및 고유 파일명 생성
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;
            String storedPath = uploadDir + savedFilename;

            // 🚨 타점 1: 하드디스크에 파일 물리적 기록
            Path path = Paths.get(storedPath);
            Files.write(path, file.getBytes());

            // 🚨 타점 2: 팀원의 DB 규격에 맞춰 엔티티 조립
            FileAttachment attachment = FileAttachment.builder()
                    .uploader(uploader)
                    .targetType(targetType) // 예: "STORE" 또는 "REVIEW"
                    .targetId(targetId)     // 예: 15 (가게 ID 또는 리뷰 ID)
                    .originalName(originalName)
                    .storedPath(storedPath) // 하드디스크 절대 경로
                    .extension(extension)
                    .fileSize(file.getSize())
                    .build();

            return fileAttachmentRepository.save(attachment).getFileId();

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}