package com.example.haksikmokjang.community.comment.service;

import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.dto.CommentCreateRequest;
import com.example.haksikmokjang.community.comment.repository.CommentRepository;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;

    //댓글 / 대댓글 작성
    @Transactional
    public Long createComment(Long postId, String loginId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // 대댓글인 경우 부모 댓글 존재 여부 팩트 체크
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(userProfile.getMember())
                .parentComment(parentComment)
                .content(request.getContent())
                .anonymousYn(request.getAnonymousYn())
                .build();

        return commentRepository.save(comment).getCommentId();
    }

    //댓글 수정
    @Transactional
    public void updateComment(Long commentId, String loginId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        //작성자 검증 방어벽
        if (!comment.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        comment.updateContent(newContent);
    }

    //댓글 삭제 (Soft Delete)
    @Transactional
    public void deleteComment(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        //작성자 검증 방어벽
        if (!comment.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        comment.delete();
    }
}