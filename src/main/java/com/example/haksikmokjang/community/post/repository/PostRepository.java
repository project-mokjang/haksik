package com.example.haksikmokjang.community.post.repository;


import com.example.haksikmokjang.community.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    List<Post> findTop2ByStatusAndBoardTypeOrderByViewCountDesc(
            com.example.haksikmokjang.community.post.domain.PostStatus status,
            com.example.haksikmokjang.community.post.domain.BoardType boardType
    );
}
