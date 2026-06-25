package com.example.haksikmokjang.community.comment.controller;

import com.example.haksikmokjang.community.comment.dto.CommentCreateRequest;
import com.example.haksikmokjang.community.comment.dto.CommentUpdateRequest;
import com.example.haksikmokjang.community.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //댓글 / 대댓글 작성 (POST)
    //댓글은 무조건 '특정 게시글'에 종속되므로 주소가 /posts/{postId}/comments 입니다.
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> createComment(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommentCreateRequest request) {

        String loginId = authentication.getName();
        Long commentId = commentService.createComment(postId, loginId, request);

        return ResponseEntity.ok("댓글이 성공적으로 등록되었습니다. Comment ID: " + commentId);
    }

    //댓글 수정 (PUT)
    //수정은 특정 댓글(commentId)을 다이렉트로 타격하므로 주소가 다릅니다.
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @RequestBody CommentUpdateRequest request) {

        String loginId = authentication.getName();
        commentService.updateComment(commentId, loginId, request.getContent());

        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }

    //댓글 삭제 (DELETE)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String loginId = authentication.getName();
        commentService.deleteComment(commentId, loginId);

        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }
}