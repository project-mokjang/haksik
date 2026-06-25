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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sort) { // 기본적으로 10개씩 가져옴

        String loginId = authentication.getName();
        if (boardType == null) boardType = BoardType.GLOBAL;

        //비즈니스 로직 호출 시 맨 끝에 sort 토스
        List<PostListResponse> response = postService.getPostList(
                loginId, boardType, category, keyword, lastPostId, size, sort);

        return ResponseEntity.ok(response);
    }

    //게시글 쓰기 (POST)
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
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        String loginId = authentication.getName();

        // 🚨 쿠키를 뜯어서 오늘 이 글을 본 적 있는지 팩트 체크
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        boolean isViewed = false;
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (cookie.getName().equals("postView_" + postId)) {
                    isViewed = true;
                    break;
                }
            }
        }

        // 🚨 본 적이 없다면(isViewed == false) 조회수를 올리고, 24시간짜리 쿠키 발급
        if (!isViewed) {
            postService.increaseViewCount(postId);
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("postView_" + postId, "true");
            cookie.setPath("/api/posts/" + postId);
            cookie.setMaxAge(60 * 60 * 24); // 24시간 유지
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(postService.getPostDetail(postId, loginId));
    }

    // 게시글 수정 (PUT)
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePost(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @ModelAttribute PostUpdateRequest request) { // @RequestBody ➔ @ModelAttribute 교체

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

    // 🚨 팩트: Authentication 객체를 통해 현재 접속자의 loginId를 Service로 넘깁니다.
    @GetMapping("/top/hot")
    public ResponseEntity<List<PostListResponse>> getHotPosts(
            @RequestParam String boardType,
            org.springframework.security.core.Authentication auth) {

        return ResponseEntity.ok(postService.getHotPosts(boardType, auth.getName()));
    }

}