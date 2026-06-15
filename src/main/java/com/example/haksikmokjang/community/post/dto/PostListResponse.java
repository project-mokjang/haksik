package com.example.haksikmokjang.community.post.dto;

import com.example.haksikmokjang.community.post.domain.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long postId;
    private String category;      // 말머리 (자유, 맛집 등)
    private String title;         // 글 제목
    private String authorName;    // 작성자 닉네임 (또는 익명)
    private int viewCount;        // 조회수
    private LocalDateTime createdAt; // 작성 시간 //후에 댓글 개수, 썸넬 이미지 url 등등 덧붙일 예정 - 진우

    //Enum을 다이렉트로 꽂아버림 ㄹㅇㅋㅋ
    public PostListResponse(Long postId, PostCategory category, String title, String authorName, int viewCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.category = (category != null) ? category.name() : "일반";
        this.title = title;
        this.authorName = authorName;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }
}
