package com.example.haksikmokjang.domain.badge;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BADGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "badge_name", nullable = false, unique = true, length = 50)
    private String badgeName;

    @Column(name = "condition_text", nullable = false, length = 255)
    private String conditionText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 뱃지 생성
    public Badge(String badgeName, String conditionText) {
        this.badgeName = badgeName;
        this.conditionText = conditionText;
        this.createdAt = LocalDateTime.now();
    }
}
