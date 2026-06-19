package com.example.haksikmokjang.chat.chatreview.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ChatReviewUserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByMember(Member member);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UserProfile u
        set u.mannerTemperature = :mannerTemperature,
            u.noShowCount = :noShowCount
        where u.member = :member
    """)
    void updateTrustValues(
            @Param("member") Member member,
            @Param("mannerTemperature") BigDecimal mannerTemperature,
            @Param("noShowCount") Integer noShowCount
    );
}
