package com.example.haksikmokjang.member.terms.repository;

import com.example.haksikmokjang.member.terms.domain.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByOrderByEffectiveAtDesc();
}
