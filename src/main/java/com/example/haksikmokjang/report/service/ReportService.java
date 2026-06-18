package com.example.haksikmokjang.report.service;

import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.repository.CommentRepository;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.dto.ReportRequest;
import com.example.haksikmokjang.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void createReport(String loginId, ReportRequest request) {
        UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        //중복 신고 방어 로직 (이미 찔렀는지 체크)
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(
                userProfile.getMember(), request.getTargetType(), request.getTargetId())) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        //내 글/내 댓글 자진 신고 방어 로직
        if ("POST".equals(request.getTargetType())) {
            Post post = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
            if (post.getMember().getLoginId().equals(loginId)) {
                throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
            }
        } else if ("COMMENT".equals(request.getTargetType())) {
            Comment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
            if (comment.getMember().getLoginId().equals(loginId)) {
                throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
            }
        }

        //체크 통과 완료. 신고 접수
        Report report = Report.builder()
                .reporter(userProfile.getMember())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }

}
