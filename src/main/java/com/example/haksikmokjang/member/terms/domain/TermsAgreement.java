package com.example.haksikmokjang.member.terms.domain;

import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(
        name = "TERMS_AGREEMENT",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_terms_agreement_member_terms",
                        columnNames = {"member_id", "terms_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(name = "agreed_yn", nullable = false, length = 1)
    private String agreedYn;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;
}
