package com.example.haksikmokjang.fileattachment.dto;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileAttachmentResponse {

    private Long fileId;
    private String originalName;
    private String extension;
    private Long fileSize;
    private String imageUrl;

    public static FileAttachmentResponse from(FileAttachment file) {
        return FileAttachmentResponse.builder()
                .fileId(file.getFileId())
                .originalName(file.getOriginalName())
                .extension(file.getExtension())
                .fileSize(file.getFileSize())
                .imageUrl("/api/images/" + file.getFileId())
                .build();
    }
}