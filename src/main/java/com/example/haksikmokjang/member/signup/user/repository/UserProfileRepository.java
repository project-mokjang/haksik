package com.example.haksikmokjang.member.signup.user.repository;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.school.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {


    boolean existsByNickname(String nickname);

    Optional<UserProfile> findByMember(Member member);

    Optional<UserProfile> findByMember_MemberId(Long memberId);


    Optional<UserProfile> findByMember_LoginId(String loginId); // 이거는 커뮤니티 게시판에서 유저 프로필좀 쓰려고 박아놨어옹 -진우

    // 이름(name)과 연결된 Member의 이메일(email)로 유저 프로필 찾기
    Optional<UserProfile> findByNameAndMember_Email(String name, String email);


    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query
            ("UPDATE UserProfile u SET u.school = :school WHERE u.member = :member")
    void updateSchoolByMember(@org.springframework.data.repository.query.Param("member")
                              Member member,
                              @org.springframework.data.repository.query.Param("school")
                              School school);

    @Modifying
    @Query("UPDATE UserProfile u SET u.profileImage = :profileImage WHERE u.member = :member")
    void updateProfileImageByMember(@Param("member") Member member,
                                    @Param("profileImage") FileAttachment profileImage);






}
