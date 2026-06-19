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

        String userMessage = requestData.get("message");


        String aiAnswer = chatbotService.getAiResponse(userMessage);


        Map<String, String> response = new HashMap<>();
        response.put("answer", aiAnswer);

        return response;
    }
}