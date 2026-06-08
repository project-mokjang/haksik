package com.example.haksikmokjang.domain.terms;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TERMS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long termsId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "content", nullable = false, length = 3000) // 3000자 정도로 넉넉하게!
    private String content;


    @Column(name = "required_yn", nullable = false, length = 1)
    private String requiredYn;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;


}
