package com.example.haksikmokjang.member.core.repository;

import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, Long> {

    Optional<MemberLocation> findByUserProfile(UserProfile userProfile);

    Optional<MemberLocation> findByUserProfile_UserProfileId(Long userProfileId);
}
