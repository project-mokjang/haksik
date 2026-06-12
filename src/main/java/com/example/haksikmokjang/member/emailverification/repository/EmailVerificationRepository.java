package com.example.haksikmokjang.member.emailverification.repository;

import com.example.haksikmokjang.member.emailverification.domain.EmailPurpose;
import com.example.haksikmokjang.member.emailverification.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            EmailPurpose purpose
    );
}
