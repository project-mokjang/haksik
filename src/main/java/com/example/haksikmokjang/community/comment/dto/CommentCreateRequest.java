package com.example.haksikmokjang.community.comment.dto;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class CommentCreateRequest {

    private Long parentCommentId; // 🚨 일반 댓글이면 null, 대댓글이면 부모 댓글의 ID가 들어옴

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;

    private String anonymousYn = "N"; // 기본값 N
}