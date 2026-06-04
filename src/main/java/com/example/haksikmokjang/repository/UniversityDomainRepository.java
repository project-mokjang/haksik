package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.school.UniversityDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, Long> {
    // 팩트: 나중에 회원가입 로직에서 쓸 '도메인 존재 여부 확인용' 근육을 미리 파둡니다.
    boolean existsByEmailDomain(String emailDomain);
}