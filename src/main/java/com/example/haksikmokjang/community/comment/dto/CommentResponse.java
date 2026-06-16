package com.example.haksikmokjang.community.comment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long parentCommentId; //대댓글 구분용
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private boolean isMine; //프론트에서 [수정/삭제] 버튼 노출 여부를 결정하는 보안 키
}
