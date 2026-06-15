package com.example.haksikmokjang.community.comment.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class CommentUpdateRequest {
    @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
    private String content;
}