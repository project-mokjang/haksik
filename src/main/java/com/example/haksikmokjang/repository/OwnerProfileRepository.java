package com.example.haksikmokjang.repository;


import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.owner.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {

    boolean existsByBusinessNumber(String businessNumber);

    Optional<OwnerProfile> findByMember(Member member);
}
