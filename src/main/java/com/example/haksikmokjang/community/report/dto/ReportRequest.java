package com.example.haksikmokjang.community.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    @NotBlank(message = "신고 대상 유형을 입력해주세요.")
    private String targetType; // "POST" or "COMMENT"

    @NotNull(message = "신고 대상 ID를 입력해주세요.")
    private Long targetId;

    @NotBlank(message = "신고 사유를 선택해주세요.")
    private String reason;
}
