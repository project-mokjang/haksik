package com.example.haksikmokjang.report.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportProcessRequest {

    //  DB 테이블 규격에 맞춰 신고 승인 및 리뷰 삭제 또는 신고 반려
    @NotBlank(message = "처리 상태값은 필수입니다.")
    private String processStatus;
}