package com.example.haksikmokjang.chatbot.controller;

import com.example.haksikmokjang.chatbot.service.ChatbotService;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    // 유저 프로필(DB)을 뒤지기 위해 창고 열쇠(Repository) 추가
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/ask")
    public Map<String, String> askToAi(
            @RequestBody Map<String, String> requestData,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // 현재 챗봇에 말 건 사람이 누군지 정보 가져오기

        // 프론트에서 보낸 질문 쏙 빼기
        String userMessage = requestData.get("message");

        //  유저의 선호 음식을 찾아서 꺼내오기 로직
        String preferredFood = "";
        if (userDetails != null && userDetails.getMember() != null) {
            Optional<UserProfile> profileOpt = userProfileRepository.findByMember(userDetails.getMember());

            // 프로필이 있고, 선호 음식도 설정해 뒀다면 꺼내오기
            if (profileOpt.isPresent() && profileOpt.get().getPreferredFoodCategory() != null) {
                preferredFood = profileOpt.get().getPreferredFoodCategory();
            }
        }

        //  챗봇 두뇌로 질문이랑 사용자 식성을 같이 던져주기
        String aiAnswer = chatbotService.getAiResponse(userMessage, preferredFood);

        // 프론트가 data.answer 로 읽을 수 있게 돌려주기
        Map<String, String> response = new HashMap<>();
        response.put("answer", aiAnswer);

        return response;
    }
}