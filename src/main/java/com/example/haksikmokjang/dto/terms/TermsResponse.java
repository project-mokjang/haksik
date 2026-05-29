package com.example.haksikmokjang.dto.terms;

import com.example.haksikmokjang.domain.terms.Terms;
import lombok.Getter;

@Getter
public class TermsResponse {
    private Long termsId;
    private String title;
    private String content;
    private String requiredYn;

    //엔티티를 받아서 DTO로 변환
    public TermsResponse(Terms terms) {
        this.termsId = terms.getTermsId();
        this.title = terms.getTitle();
        this.content = terms.getContent();
        this.requiredYn = terms.getRequiredYn();
    }
}