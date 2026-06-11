package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.fileattachment.FileAttachment;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.domain.school.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByMember_MemberId(Long memberId);

    boolean existsByNickname(String nickname);

    Optional<UserProfile> findByMember(Member member);

    Optional<UserProfile> findByMember_MemberId(Long memberId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query
            ("UPDATE UserProfile u SET u.school = :school WHERE u.member = :member")
    void updateSchoolByMember(@org.springframework.data.repository.query.Param("member")
                              com.example.haksikmokjang.domain.member.Member member,
                              @org.springframework.data.repository.query.Param("school")
                              com.example.haksikmokjang.domain.school.School school);

    @Modifying
    @Query("UPDATE UserProfile u SET u.profileImage = :profileImage WHERE u.member = :member")
    void updateProfileImageByMember(@Param("member") Member member,
                                    @Param("profileImage") FileAttachment profileImage);


}
