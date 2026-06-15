package com.example.haksikmokjang.community.post.service;

import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.dto.PostCreateRequest;
import com.example.haksikmokjang.community.post.dto.PostDetailResponse;
import com.example.haksikmokjang.community.post.dto.PostListResponse;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.school.domain.School;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 단순 조회일 땐 readOnly만 걸어 DB 부하 줄임.
public class PostService {

    private final PostRepository postRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    public List<PostListResponse> getPostList(
            String loginId,
            BoardType boardType,
            PostCategory category,
            String keyword,
            Long lastPostId,
            int pageSize) {

        Long schoolId = null;

        if (boardType == BoardType.SCHOOL) {
            //Exception 교체 타점 1
            UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));
            schoolId = userProfile.getSchool().getSchoolId();
        }

        return postRepository.searchPosts(boardType, schoolId, category, keyword, lastPostId, pageSize);
    }

    @Transactional
    public Long createPost(String loginId, PostCreateRequest request) {
        //Exception
        UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // School targetSchool
        School targetSchool = null;
        if (request.getBoardType() == BoardType.SCHOOL) {
            targetSchool = userProfile.getSchool();
        }

        //Post.builder()
        Post newPost = Post.builder()
                .member(userProfile.getMember())
                .school(targetSchool)
                .boardType(request.getBoardType())
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .anonymousYn(request.getAnonymousYn())
                .build();

        Post savedPost = postRepository.save(newPost);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            savePostImages(savedPost, request.getImages());
        }

        return savedPost.getPostId();
    }

    private void savePostImages(Post post, List<MultipartFile> images) {

        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String storedFilename = UUID.randomUUID().toString() + extension;
            String storedPath = uploadDir + "/" + storedFilename;

            try {
                file.transferTo(new File(storedPath));

                FileAttachment attachment = FileAttachment.builder()
                        .uploader(post.getMember())
                        .targetType("POST")
                        .targetId(post.getPostId())
                        .originalName(originalFilename)
                        .storedPath(storedPath)
                        .extension(extension.replace(".", ""))
                        .fileSize(file.getSize())
                        .build();

                fileAttachmentRepository.save(attachment);

            } catch (IOException e) {
                // 🚨 Exception 교체 타점 3
                throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long postId) {
        // 🚨 하드코딩 제거 및 Exception 교체 타점 4
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.addViewCount();

        String authorName = "익명";
        if (post.getAnonymousYn().equals("N")) {
            // 🚨 Exception 교체 타점 5
            UserProfile profile = userProfileRepository.findByMember(post.getMember())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            authorName = profile.getNickname();
        }

        List<FileAttachment> attachments = fileAttachmentRepository.findByTargetTypeAndTargetId("POST", postId);
        List<String> imageUrls = attachments.stream()
                .map(file -> "/api/images/" + file.getFileId())
                .toList();

        PostDetailResponse response = new PostDetailResponse();
        response.setPostId(post.getPostId());
        response.setCategory(post.getCategory().name());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorName(authorName);
        response.setViewCount(post.getViewCount());
        response.setCreatedAt(post.getCreatedAt());
        response.setImageUrls(imageUrls);

        return response;
    }
}