package com.example.haksikmokjang.chatbot.controller;

import com.example.haksikmokjang.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;



    @PostMapping("/ask")
    public Map<String, String> askToAi(@RequestBody Map<String, String> requestData) {
        // 프론트에서 보낸 { "message": "질문내용" } 에서 질문만 쏙 빼기
        String userMessage = requestData.get("message");

        // AI(이제는 Ollama!)한테 물어보고 답변 받아오기
        String aiAnswer = chatbotService.getAiResponse(userMessage);

        // 프론트가 data.answer 로 읽을 수 있게 돌려주기
        Map<String, String> response = new HashMap<>();
        response.put("answer", aiAnswer);

        return response;
    }
}