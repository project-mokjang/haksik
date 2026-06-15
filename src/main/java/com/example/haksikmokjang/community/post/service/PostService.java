package com.example.haksikmokjang.service.community.post;

import com.example.haksikmokjang.dto.community.post.PostDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.example.haksikmokjang.domain.fileattachment.FileAttachment;
import com.example.haksikmokjang.repository.FileAttachmentRepository;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import com.example.haksikmokjang.domain.community.post.BoardType;
import com.example.haksikmokjang.domain.community.post.PostCategory;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.dto.community.post.PostCreateRequest;
import com.example.haksikmokjang.dto.community.post.PostListResponse;
import com.example.haksikmokjang.repository.UserProfileRepository;
import com.example.haksikmokjang.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.haksikmokjang.domain.community.post.QPost.post;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //단순 조회일땐 readOnly만 걸어 DB 부하 줄임.
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

        // '우리 학교' 게시판일 경우에만, 백엔드에서 유저의 진짜 학교 ID를 꺼내옵니다.
        if (boardType == BoardType.SCHOOL) {
            UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
            schoolId = userProfile.getSchool().getSchoolId();
        }

        // 쿼리 호출
        return postRepository.searchPosts(boardType, schoolId, category, keyword, lastPostId, pageSize);
    } // 🚨 팩트 체크: 여기서 getPostList 메서드가 정확하게 닫혀야 합니다!

    @Transactional // 🚨 팩트: DB에 데이터(INSERT)를 꽂아 넣으므로 여기만 readOnly = false (기본값) 덮어쓰기
    public Long createPost(String loginId, PostCreateRequest request) {
        // 1. 작성자의 프로필과 뼈대(Member)를 가져옵니다.
        UserProfile userProfile = userProfileRepository.findByMember_LoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 프로필이 존재하지 않습니다."));

        // 2. 소속 학교 세팅 (전국구면 null, 우리학교면 유저의 학교 정보 꽂기)
        com.example.haksikmokjang.domain.school.School targetSchool = null;
        if (request.getBoardType() == BoardType.SCHOOL) {
            targetSchool = userProfile.getSchool();
        }

        // 3. Post 엔티티 뼈대 조립
        com.example.haksikmokjang.domain.community.post.Post newPost = com.example.haksikmokjang.domain.community.post.Post.builder()
                .member(userProfile.getMember())
                .school(targetSchool)
                .boardType(request.getBoardType())
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .anonymousYn(request.getAnonymousYn())
                .build();

        // 4. DB 창고에 1차 저장 (이때 postId가 자동 생성되어 꽂힘)
        com.example.haksikmokjang.domain.community.post.Post savedPost = postRepository.save(newPost);

        // 5. 이미지 파일 업로드 처리
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            savePostImages(savedPost, request.getImages());
        }

        return savedPost.getPostId();
    }

    private void savePostImages(com.example.haksikmokjang.domain.community.post.Post post, List<MultipartFile> images) {

        // 1. 저장할 폴더가 컴퓨터에 없으면 자동으로 생성 (기본 바닥 공사)
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 2. 넘어온 이미지 개수만큼 반복하며 저장
        for (MultipartFile file : images) {
            if (file.isEmpty()) continue; // 빈 파일 방어

            // 원래 파일명 (예: 내사진.png)
            String originalFilename = file.getOriginalFilename();

            // 확장자 추출 (예: .png)
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 🚨 핵심 팩트: 똑같은 이름의 파일이 덮어씌워지는 걸 막기 위해 무작위 난수(UUID) 생성
            // 예: 123e4567-e89b-12d3-a456-426614174000.png
            String storedFilename = UUID.randomUUID().toString() + extension;

            // 최종 저장될 절대 경로
            String storedPath = uploadDir + "/" + storedFilename;

            try {
                // 3. 실제 컴퓨터 하드디스크(C:/Sibal/haksik/...)에 파일을 내려받아 저장 (원판 꽂기)
                file.transferTo(new File(storedPath));

                // 4. DB에 주소표(FileAttachment) 조립해서 꽂아 넣기
                FileAttachment attachment = FileAttachment.builder()
                        .uploader(post.getMember())
                        .targetType("POST")
                        .targetId(post.getPostId())
                        .originalName(originalFilename)
                        .storedPath(storedPath)
                        .extension(extension.replace(".", "")) // 점(.) 빼고 순수 확장자만
                        .fileSize(file.getSize())
                        .build();

                fileAttachmentRepository.save(attachment);

            } catch (IOException e) {
                // 디스크 용량이 꽉 찼거나 권한이 없으면 여기서 터집니다
                throw new RuntimeException("파일 업로드 중 치명적인 부상이 발생했습니다.", e);
            }
        }
    }

    @Transactional // 🚨 팩트: addViewCount()로 DB 값이 변하므로 readOnly=false여야 합니다.
    public PostDetailResponse getPostDetail(Long postId) {
        //뼈대 찾아오기
        com.example.haksikmokjang.domain.community.post.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        //조회수 1 펌핑
        post.addViewCount();

        //작성자 닉네임 꺼내오기 (익명 처리 적용)
        String authorName = "익명";
        if (post.getAnonymousYn().equals("N")) {
            UserProfile profile = userProfileRepository.findByMember(post.getMember())
                    .orElseThrow(() -> new IllegalArgumentException("작성자 프로필이 없습니다."));
            authorName = profile.getNickname();
        }

        //글에 달린 사진들의 URL 리스트 만들기
        java.util.List<FileAttachment> attachments = fileAttachmentRepository.findByTargetTypeAndTargetId("POST", postId);
        java.util.List<String> imageUrls = attachments.stream()
                .map(file -> "/api/images/" + file.getFileId()) // 방금 만든 ImageController 주소로 매핑
                .toList();

        //DTO 담기
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