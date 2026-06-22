package com.example.haksikmokjang.member.reivew.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateRequest {
    @NotNull(message = "별점은 필수입니다.")
    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;
}