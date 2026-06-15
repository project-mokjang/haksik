package com.example.haksikmokjang.community.post.dto;
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
}
