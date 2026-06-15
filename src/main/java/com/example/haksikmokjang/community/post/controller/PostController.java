package com.example.haksikmokjang.community.post.controller;


import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.dto.PostCreateRequest;
import com.example.haksikmokjang.community.post.dto.PostDetailResponse;
import com.example.haksikmokjang.community.post.dto.PostListResponse;
import com.example.haksikmokjang.community.post.dto.PostUpdateRequest;
import com.example.haksikmokjang.community.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostListResponse>> getPosts(
            Authentication authentication, // 시큐리티를 통과한 현재 로그인 유저 정보
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size) { // 기본적으로 10개씩 가져옴

        // 로그인한 유저의 ID 추출 (시큐리티 설정에 따라 username이 loginId로 들어갑니다)
        String loginId = authentication.getName();

        // 파라미터가 비어있으면 기본적으로 전국구(GLOBAL) 게시판을 띄웁니다.
        if (boardType == null) {
            boardType = BoardType.GLOBAL;
        }

        // 비즈니스 로직 호출
        List<PostListResponse> response = postService.getPostList(
                loginId, boardType, category, keyword, lastPostId, size);

        // JSON 형태로 깔끔하게 리턴
        return ResponseEntity.ok(response);
    }

    // 🏋️‍♂️ 2. 게시글 쓰기 (POST) - 🚨 이 부분이 없어서 아까 500 에러가 터진 겁니다!
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPost(
            Authentication authentication,
            @Valid @ModelAttribute PostCreateRequest request) {

        String loginId = authentication.getName();

        // 서비스 로직 호출 및 생성된 게시글 ID 반환
        Long savedPostId = postService.createPost(loginId, request);

        return ResponseEntity.ok("게시글이 성공적으로 등록되었습니다. Post ID: " + savedPostId);
    }
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }
    // 게시글 수정 (PUT)
    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody PostUpdateRequest request) {

        String loginId = authentication.getName();
        postService.updatePost(postId, loginId, request);

        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
    }

    //게시글 삭제 (DELETE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String loginId = authentication.getName();
        postService.deletePost(postId, loginId);

        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }
}