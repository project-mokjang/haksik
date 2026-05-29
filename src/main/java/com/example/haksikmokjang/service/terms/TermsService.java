package com.example.haksikmokjang.service.terms;

import com.example.haksikmokjang.dto.terms.TermsResponse;
import com.example.haksikmokjang.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    @Transactional(readOnly = true)
    public List<TermsResponse> getTermsList() {
        // DB에서 약관을 긁어와서 TermsResponse 리스트로 반환
        return termsRepository.findAllByOrderByEffectiveAtDesc().stream()
                .map(TermsResponse::new)
                .collect(Collectors.toList());
    }
}