package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.fileattachment.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
}
