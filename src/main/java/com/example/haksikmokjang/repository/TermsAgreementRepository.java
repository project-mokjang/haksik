package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.terms.Terms;
import com.example.haksikmokjang.domain.terms.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    boolean existsByMemberAndTerms(Member member, Terms terms);

    void deleteByMember(com.example.haksikmokjang.domain.member.Member member);
}
