package com.example.haksikmokjang.domain.school;

import com.example.haksikmokjang.domain.common.CreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SCHOOL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class School extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "school_name", nullable = false, length = 100)
    private String schoolName;

    @Column(name = "email_domain", nullable = false, unique = true, length = 100)
    private String emailDomain;
}
