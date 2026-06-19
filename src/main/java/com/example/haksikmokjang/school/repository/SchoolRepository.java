package com.example.haksikmokjang.school.repository;

import com.example.haksikmokjang.school.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByEmailDomain(String emailDomain);

    Optional<School> findBySchoolName(String schoolName);
}
