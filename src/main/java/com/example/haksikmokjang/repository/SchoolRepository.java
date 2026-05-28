package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.school.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
