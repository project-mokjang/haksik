package com.example.haksikmokjang.community.comment.repository;
import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.domain.CommentStatus;
import com.example.haksikmokjang.community.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    //게시글에 달린 댓글 중 삭제되지 않은(ACTIVE) 것만 다 가져옴
    List<Comment> findByPostAndStatusOrderByCommentIdAsc(Post post, CommentStatus status);
}