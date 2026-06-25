package com.example.haksikmokjang.fileattachment.repository;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    void deleteByUploader(Member uploader);
    java.util.List<FileAttachment> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
