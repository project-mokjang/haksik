package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.terms.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByOrderByEffectiveAtDesc();
}
