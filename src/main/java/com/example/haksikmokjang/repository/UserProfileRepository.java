package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByNickname(String nickname);

    Optional<UserProfile> findByMember(Member member);
}
