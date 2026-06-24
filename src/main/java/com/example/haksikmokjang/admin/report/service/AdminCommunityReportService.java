package com.example.haksikmokjang.admin.report.service;

import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.repository.CommentRepository;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.report.domain.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommunityReportService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    // 게시글 신고 상세 조회
    public AdminReportDetailResponse getPostReportDetail(Report report) {
        Post post = postRepository.findById(report.getTargetId())
                .orElse(null);

        if (post == null) {
            return AdminReportDetailResponse.from(
                    report,
                    "존재하지 않는 게시글",
                    "신고 대상 게시글이 삭제되었거나 존재하지 않습니다.",
                    null,
                    "NOT_FOUND"
            );
        }

        return AdminReportDetailResponse.from(
                report,
                post.getTitle(),
                post.getContent(),
                post.getMember().getLoginId(),
                post.getStatus().name()
        );
    }

    // 댓글 신고 상세 조회
    public AdminReportDetailResponse getCommentReportDetail(Report report) {
        Comment comment = commentRepository.findById(report.getTargetId())
                .orElse(null);

        if (comment == null) {
            return AdminReportDetailResponse.from(
                    report,
                    "존재하지 않는 댓글",
                    "신고 대상 댓글이 삭제되었거나 존재하지 않습니다.",
                    null,
                    "NOT_FOUND"
            );
        }

        return AdminReportDetailResponse.from(
                report,
                "댓글",
                comment.getContent(),
                comment.getMember().getLoginId(),
                comment.getStatus().name()
        );
    }

    // 커뮤니티 신고 대상 숨김 처리
    @Transactional
    public void hideCommunityTarget(Report report) {
        if ("POST".equals(report.getTargetType())) {
            Post post = postRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            post.hide();
            return;
        }

        if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            comment.hide();
        }
    }

    // 커뮤니티 신고 대상 작성자 조회
    public Member findCommunityTargetWriter(Report report) {
        if ("POST".equals(report.getTargetType())) {
            return postRepository.findById(report.getTargetId())
                    .map(Post::getMember)
                    .orElse(null);
        }

        if ("COMMENT".equals(report.getTargetType())) {
            return commentRepository.findById(report.getTargetId())
                    .map(Comment::getMember)
                    .orElse(null);
        }

        return null;
    }

    // 신고 대상 커뮤니티 상태 조회
    public String getCommunityTargetStatus(Report report) {
        if ("POST".equals(report.getTargetType())) {
            return postRepository.findById(report.getTargetId())
                    .map(post -> post.getStatus().name())
                    .orElse("NOT_FOUND");
        }

        if ("COMMENT".equals(report.getTargetType())) {
            return commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getStatus().name())
                    .orElse("NOT_FOUND");
        }

        return null;
    }

    // 신고 처리 취소 시 커뮤니티 대상 상태 복구
    @Transactional
    public void restoreCommunityTarget(Report report, String beforeTargetStatus) {
        if (beforeTargetStatus == null || "NOT_FOUND".equals(beforeTargetStatus)) {
            return;
        }

        if ("POST".equals(report.getTargetType())) {
            Post post = postRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

            post.restoreStatus(beforeTargetStatus);
            return;
        }

        if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            comment.restoreStatus(beforeTargetStatus);
        }
    }
}