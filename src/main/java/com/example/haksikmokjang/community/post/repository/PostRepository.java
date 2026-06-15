package com.example.haksikmokjang.community.post.repository;


import com.example.haksikmokjang.community.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
