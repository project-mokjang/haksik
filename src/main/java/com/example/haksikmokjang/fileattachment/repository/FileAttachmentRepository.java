package com.example.haksikmokjang.fileattachment.repository;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
}
