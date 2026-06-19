package com.example.haksikmokjang.ownerpage.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewReportRequest {
    @NotBlank(message = "신고 사유를 작성해야 합니다.")
    private String reason; // 예: "욕설이 포함되어 있습니다."
}