package com.example.haksikmokjang.fileattachment.service;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    //private final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Transactional
    public Long uploadFile(String loginId, MultipartFile file, String targetType, Long targetId) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        Member uploader = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        try {
            // 폴더 없으면 자동 생성 방어벽
//            File directory = new File(uploadDir);
////            if (!directory.exists()) {
////                directory.mkdirs();
////            }

            String originalName = file.getOriginalFilename();
            if (originalName == null) throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);

            // 확장자 분리 (".jpg") 및 고유 파일명 생성
            String extension = getExtension(originalName);
            String savedFilename = UUID.randomUUID() + extension;

            // 🚨 팩트: 절대 경로를 쓸 때는 쓸데없는 현재 디렉토리 호출을 완전히 제거해야 합니다.
            Path uploadPath = Path.of(
                    uploadDir,
                    getUploadFolder(targetType)
            );

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(savedFilename);

            Files.write(filePath, file.getBytes());

            // 🚨 타점 2: 팀원의 DB 규격에 맞춰 엔티티 조립
            FileAttachment attachment = FileAttachment.builder()
                    .uploader(uploader)
                    .targetType(targetType) // 예: "STORE" 또는 "REVIEW"
                    .targetId(targetId)     // 예: 15 (가게 ID 또는 리뷰 ID)
                    .originalName(originalName)
                    .storedPath(filePath.toAbsolutePath().toString()) // 하드디스크 절대 경로
                    .extension(extension)
                    .fileSize(file.getSize())
                    .build();

            return fileAttachmentRepository.save(attachment).getFileId();

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    // targetType별 업로드 폴더명 결정
    private String getUploadFolder(String targetType) {
        if ("POST".equals(targetType)) {
            return "post";
        }

        if ("COMMENT".equals(targetType)) {
            return "comment";
        }

        if ("REVIEW".equals(targetType)) {
            return "review";
        }

        if ("CHAT_MESSAGE".equals(targetType)) {
            return "chat";
        }
        if ("USER_PROFILE".equals(targetType)) {
            return "profile";
        }
        if ("STORE".equals(targetType)) {
            return "store";
        }

        if ("MENU".equals(targetType)) {
            return "menu";
        }

        return "etc";
    }

    // 파일 확장자 추출
    private String getExtension(String originalName) {
        if (!originalName.contains(".")) {
            return "";
        }

        return originalName.substring(originalName.lastIndexOf("."));
    }

    @Transactional
    public void deleteFile(Long fileId) {
        FileAttachment attachment = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. ID: " + fileId));

        // 1. 서버 하드디스크 물리 경로 추적 및 파일 사살
        try {
            // 🚨 팩트: 회원님의 엔티티 규격에 맞춰 getStoredPath()로 정확히 타겟팅합니다.
            Path filePath = Paths.get(attachment.getStoredPath());
            Files.deleteIfExists(filePath); // 파일이 존재하면 즉각 삭제
        } catch (IOException e) {
            // 팩트: 하드디스크 찌꺼기 누수를 막기 위해 예외를 던져 트랜잭션을 강제 롤백시킵니다.
            throw new RuntimeException("물리 파일 삭제 중 치명적 오류가 발생했습니다.", e);
        }

        // 2. 물리 파일 삭제 성공 시, DB에서 레코드 삭제
        fileAttachmentRepository.delete(attachment);
    }
}