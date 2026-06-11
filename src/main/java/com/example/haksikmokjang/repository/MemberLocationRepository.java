package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.MemberLocation;
import com.example.haksikmokjang.domain.member.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, Long> {

    Optional<MemberLocation> findByUserProfile(UserProfile userProfile);

    Optional<MemberLocation> findByUserProfile_UserProfileId(Long userProfileId);

    void deleteByUserProfile(com.example.haksikmokjang.domain.member.UserProfile userProfile);
}
