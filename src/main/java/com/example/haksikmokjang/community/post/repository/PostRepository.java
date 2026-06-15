package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.community.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
