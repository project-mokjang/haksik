package com.example.haksikmokjang.community.post.service;

import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.dto.CommentResponse;
import com.example.haksikmokjang.community.comment.repository.CommentRepository;
import com.example.haksikmokjang.community.post.domain.PostStatus;
import com.example.haksikmokjang.community.post.dto.PostUpdateRequest;
import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.dto.PostCreateRequest;
import com.example.haksikmokjang.community.post.dto.PostDetailResponse;
import com.example.haksikmokjang.community.post.dto.PostListResponse;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.fileattachment.service.FileAttachmentService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.school.domain.School;
import lombok.RequiredArgsConstructor;
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
    private final CommentRepository commentRepository;
    private final FileAttachmentService fileAttachmentService;

    public List<PostListResponse> getPostList(
            String loginId,
            BoardType boardType,
            PostCategory category,
            String keyword,
            Long lastPostId,
            int pageSize,
            String sort) {

        Long schoolId = null;

        if (boardType == BoardType.SCHOOL) {
            //Exception 교체 타점 1
            UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));
            schoolId = userProfile.getSchool().getSchoolId();
        }

        return postRepository.searchPosts(boardType, schoolId, category, keyword, lastPostId, pageSize, loginId,sort);
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

    // 게시판 이미지 저장
    private void savePostImages(Post post, List<MultipartFile> images) {
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            fileAttachmentService.uploadFile(
                    post.getMember().getLoginId(),
                    file,
                    "POST",
                    post.getPostId()
            );
        }
    }

    @Transactional
    // 매개변수에 loginId 추가
    public PostDetailResponse getPostDetail(Long postId, String loginId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));


        //게시글 작성자 닉네임 세팅 (익명 방어)
        String authorName = "익명";
        if (post.getAnonymousYn().equals("N")) {
            UserProfile profile = userProfileRepository.findByMember(post.getMember())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            authorName = profile.getNickname();
        }

        //이미지 URL 세팅
        List<FileAttachment> attachments = fileAttachmentRepository.findByTargetTypeAndTargetId("POST", postId);
        List<String> imageUrls = attachments.stream()
                .map(file -> "/api/images/" + file.getFileId())
                .toList();

        //대망의 댓글 세팅 (상태가 ACTIVE인 정상 댓글만 싹 다 긁어오기)
        List<Comment> commentList =
                commentRepository.findByPostAndStatusOrderByCommentIdAsc(
                        post, com.example.haksikmokjang.community.comment.domain.CommentStatus.ACTIVE);

        List<com.example.haksikmokjang.community.comment.dto.CommentResponse> comments = commentList.stream().map(comment -> {
            String commentAuthor = "익명";
            if (comment.getAnonymousYn().equals("N")) {
                UserProfile cProfile = userProfileRepository.findByMember(comment.getMember())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
                commentAuthor = cProfile.getNickname();
            }

            // 이 댓글이 내가 쓴 댓글인지 팩트 체크
            boolean isCommentMine = comment.getMember().getLoginId().equals(loginId);
            Long parentId = comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null;

            return new CommentResponse(
                    comment.getCommentId(), parentId, commentAuthor, comment.getContent(), comment.getCreatedAt(), isCommentMine
            );
        }).toList();

        //이 게시글이 내가 쓴 게시글인지 체크
        boolean isPostMine = post.getMember().getLoginId().equals(loginId);

        //올인원 바구니 조립
        PostDetailResponse response = new PostDetailResponse();
        response.setPostId(post.getPostId());
        response.setCategory(post.getCategory().name());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorName(authorName);
        response.setViewCount(post.getViewCount());
        response.setCreatedAt(post.getCreatedAt());
        response.setImageUrls(imageUrls);
        response.setMine(isPostMine); // lombok이 boolean 필드에 대해 setMine()으로 자동 생성함
        response.setComments(comments);

        return response;
    }

    @Transactional
    public void updatePost(Long postId, String loginId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 작성자 검증 방어벽
        if (!post.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 텍스트 정보 업데이트 (Dirty Checking)
        post.updatePost(request.getTitle(), request.getContent());

        // 하드코어 사진 덮어쓰기 로직
        // 프론트에서 새로운 사진을 보냈을 때만 발동합니다.
        if (request.getImages() != null && !request.getImages().isEmpty()) {

            // 기존 이 게시글(POST)에 결속된 사진 데이터들을 싹 긁어옵니다.
            List<FileAttachment> oldFiles = fileAttachmentRepository.findByTargetTypeAndTargetId("POST", postId);

            // 물리적 하드디스크 파일 폭파
            for (FileAttachment oldFile : oldFiles) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(oldFile.getStoredPath()));
                } catch (java.io.IOException e) {
                    // 삭제 실패 시 로그만 남기고 패스 (DB 정합성이 더 중요)
                    System.err.println("기존 물리 파일 삭제 실패: " + oldFile.getStoredPath());
                }
            }

            // DB 매핑 테이블에서 기존 사진 기록 폭파
            fileAttachmentRepository.deleteAll(oldFiles);

            // 새로운 사진들 저장 및 DB 재결속
            savePostImages(post, request.getImages());
        }
    }

    @Transactional
    public void deletePost(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 🚨 작성자 검증 방어벽
        if (!post.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 상태값을 DELETED로 변경 (Soft Delete)
        post.deletePost();
    }
    // 조회수
    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.addViewCount();
    }


    // 🚨 팩트: DTO 변환 시 닉네임 처리와 내 글 여부(loginId)를 정확하게 매핑합니다.
    @Transactional(readOnly = true)
    public List<PostListResponse> getHotPosts(String boardTypeStr, String loginId) {
        BoardType boardType = BoardType.valueOf(boardTypeStr);

        List<Post> hotPosts = postRepository.findTop2ByStatusAndBoardTypeOrderByViewCountDesc(PostStatus.ACTIVE, boardType);

        return hotPosts.stream().map(post -> {
            // 1. 익명 방어벽 및 닉네임 추출
            String authorName = "익명";
            if ("N".equals(post.getAnonymousYn())) {
                UserProfile profile = userProfileRepository.findByMember(post.getMember())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));
                authorName = profile.getNickname();
            }

            // 2. 내 글인지 팩트 체크
            boolean isMine = post.getMember().getLoginId().equals(loginId);

            // 3. DTO 수동 조립 (회원님의 PostListResponse 필드명에 맞게 살짝 수정이 필요할 수 있습니다)
            PostListResponse res = new PostListResponse();
            res.setPostId(post.getPostId());
            res.setCategory(post.getCategory().name()); // DTO가 String이면 .name() 유지
            res.setTitle(post.getTitle());
            res.setAuthorName(authorName);
            res.setViewCount(post.getViewCount());
            res.setCreatedAt(post.getCreatedAt());
            res.setMine(isMine);

            return res;
        }).toList();
    }
}