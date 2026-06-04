package com.example.haksikmokjang.domain.school;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "university_domains")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UniversityDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "university_name", nullable = false, length = 100)
    private String universityName;

    @Column(name = "email_domain", nullable = false, length = 100, unique = true)
    private String emailDomain;
}