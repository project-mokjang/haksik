package com.example.haksikmokjang.domain.fileattachment;

import com.example.haksikmokjang.domain.common.CreatedTimeEntity;
import com.example.haksikmokjang.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FILE_ATTACHMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAttachment extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private Member uploader;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 255)
    private String storedPath;

    @Column(name = "extension", length = 20)
    private String extension;

    @Column(name = "file_size")
    private Long fileSize;
}
