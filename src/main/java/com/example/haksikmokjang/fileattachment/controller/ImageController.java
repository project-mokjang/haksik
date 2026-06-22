package com.example.haksikmokjang.fileattachment.controller;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.fileattachment.service.FileAttachmentService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final FileAttachmentRepository fileAttachmentRepository;
    private final FileAttachmentService fileAttachmentService;

    @GetMapping("/api/images/{fileId}")
    public ResponseEntity<Resource> showImage(@PathVariable Long fileId) throws IOException {
        //DB 탐색
        FileAttachment file = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        //하드디스크의 경로에서 파일 긁어오기
        Resource resource = new UrlResource("file:" + file.getStoredPath());

        //브라우저가 이미지(png, jpeg)로 인식하도록 헤더에 타입 꽂아주기
        String contentType = Files.probeContentType(Path.of(file.getStoredPath()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
    @PostMapping("/api/images/upload")
    public ResponseEntity<Long> uploadImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId) {

        String loginId = authentication.getName();

        // 서비스 격발
        Long fileId = fileAttachmentService.uploadFile(loginId, file, targetType, targetId);

        // 업로드된 파일의 PK(고유번호) 반환
        return ResponseEntity.ok(fileId);
    }
}
