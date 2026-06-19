package com.example.haksikmokjang.member.signup.owner.repository;


import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {

    boolean existsByBusinessNumber(String businessNumber);

    Optional<OwnerProfile> findByMember(Member member);

    List<OwnerProfile> findAllByOrderByOwnerProfileIdDesc();
}
