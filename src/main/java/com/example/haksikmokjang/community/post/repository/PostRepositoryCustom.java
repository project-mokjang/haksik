package com.example.haksikmokjang.community.post.repository;



import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.dto.PostListResponse;

import java.util.List;

public interface PostRepositoryCustom {
    //lastPostId 이 ID 번호보다 작은글 10개 가져와 라는 뜻
    List<PostListResponse> searchPosts(
            BoardType boardType,
            Long schoolId,
            PostCategory category,
            String keyword,
            Long lastPostId,
            int pageSize
    );
}
