package com.example.haksikmokjang.community.post.dto;
import com.example.haksikmokjang.community.comment.dto.CommentResponse;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDetailResponse {
    private Long postId;
    private String category;
    private String title;
    private String content;
    private String authorName;
    private int viewCount;
    private LocalDateTime createdAt;
    private List<String> imageUrls; //사진들을 화면에 띄울 API 주소

    // 🚨 새롭게 추가될 2가지 관절
    private boolean isMine; // 이 게시글 자체가 내 글인가?
    private java.util.List<CommentResponse> comments; // 댓글 목록 통째로 탑재

}
