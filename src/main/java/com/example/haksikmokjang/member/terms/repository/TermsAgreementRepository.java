package com.example.haksikmokjang.member.terms.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.terms.domain.Terms;
import com.example.haksikmokjang.member.terms.domain.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    boolean existsByMemberAndTerms(Member member, Terms terms);

    void deleteByMember(com.example.haksikmokjang.member.core.domain.Member member);
}
