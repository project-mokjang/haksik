package com.example.haksikmokjang.community.post.dto;

import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostCreateRequest {

    @NotNull(message = "게시판 타입을 선택해주세요.")
    private BoardType boardType;

    @NotNull(message = "카테고리(말머리)를 선택해주세요.")
    private PostCategory category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private String anonymousYn = "N"; // 기본값은 실명(N), 체크 시 익명(Y)

    // 첨부파일을 받을 수 있는 전용 타입. 없으면 null 드감
    private List<MultipartFile> images;
}