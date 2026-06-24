package com.example.haksikmokjang.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🚨 application.properties 나 application.yml 에 gemini.api.key=오빠키값 설정해두기!
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @SuppressWarnings("unchecked")
    public String getAiResponse(String userMessage, String preferredFood) {

        //제미나이 API 엔드포인트
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 선호 음식 귓속말 세팅
        String foodInstruction = "";
        if (preferredFood != null && !preferredFood.isEmpty()) {
            foodInstruction = "이 사용자는 현재 [" + preferredFood + "]을(를) 가장 좋아해! 만약 사용자가 음식 추천을 해달라고 하면, 반드시 이 [" + preferredFood + "] 카테고리 안에서 센스 있게 딱 1개의 메뉴만 골라서 추천해 줘!\n";
        } else {
            foodInstruction = "이 사용자는 아직 선호 음식을 설정하지 않았어. 음식 추천을 해달라고 하면, 한식, 중식, 일식, 양식 중에서 랜덤으로 맛있어 보이는 메뉴 1개를 골라 추천해 줘!\n";
        }

        String systemPrompt = "너는 인덕대학교 학생 매칭 앱 '학식목장'의 공식 고객지원 관리자입니다.\n" +
                "사용자의 질문에 대해 감정을 배제하고, 아래 [학식목장 앱 이용 가이드 및 수칙]을 바탕으로 사무적이고 정확한 정보만을 제공해 주세요.\n" +
                "모든 답변은 이모티콘 없이 정중한 존댓말(입니다/해주세요체)을 사용하며, 2줄 이내로 간결하게 요약하여 전달해 주세요.\n\n" +
                "[학식목장 앱 이용 가이드 및 수칙]\n" +
                "1. 학식메이트 매칭: '매칭' 메뉴에서 '학식메이트' 선택 후 지도에서 파트너 탐색이 가능합니다.\n" +
                "2. 소개팅 매칭: '매칭' 메뉴에서 '소개팅' 선택 후 1:1 상대 확인이 가능합니다.\n" +
                "3. 과팅 매칭: '매칭' 메뉴에서 '과팅' 선택 후 그룹 단위 상대 탐색이 가능합니다.\n" +
                "4. 빠른 메뉴: 하단 네비게이션 바([홈, 매칭, 채팅, 커뮤니티, 마이])를 통해 이동이 가능합니다.\n" +
                "5. 채팅 확인: 하단 '채팅' 메뉴에서 대화 내역 확인이 가능합니다.\n" +
                "6. 내 정보 관리: '마이' 탭을 통해 프로필 및 상세 정보 수정이 가능합니다.\n" +
                "7. 오늘의 음식 추천: " + foodInstruction + "\n" +
                "8. 비매너 신고: 커뮤니티 및 채팅방 우측 상단 점 3개 메뉴를 통해 신고 접수가 가능합니다.\n" +
                "9. 오늘의 운세: 관련 질문 시 운세 정보를 제공합니다.\n" +
                "10. 규칙 안내: 항상 공공장소에서 만나고, 매너 있는 대화를 부탁드립니다. 불편한 상황 발생 시 신고 기능을 활용해 주십시오.\n" +
                "11. 제재 사항: 비매너 행위, 광고 및 홍보, 도배, 허위 매칭은 금지되며 적발 시 제재될 수 있습니다.\n" +
                "12. 커뮤니티 주의사항: 비방, 인신공격, 정치/종교 논란 유도, 저작권 침해, 성인물/혐오 표현, 허위사실 유포는 금지됩니다.\n" +
                "13. 매칭 추천: 사용자가 매칭 추천을 요청하면 '학식메이트', '소개팅', '과팅' 중 하나를 임의로 선정하여 추천해주세요.\n" +
                "14. 외부 질문: 가이드 외의 질문은 정보 제공이 어렵다고 답변해 주십시오.\n\n" +
                "주의사항: 가이드에 없는 내용은 임의로 생성하지 말고 '해당 정보는 현재 제공이 어렵습니다'라고 명시해 주세요.";

        // 제미나이 입맛에 맞게 JSON 바디 포장하기
        Map<String, Object> requestBody = new HashMap<>();

        // 시스템 프롬프트 넣기 (Gemini 2.5 방식)
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", Map.of("text", systemPrompt));
        requestBody.put("system_instruction", systemInstruction);

        // 유저가 보낸 메시지 넣기
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("parts", List.of(Map.of("text", userMessage)));
        contents.add(userContent);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // 제미나이 응답 까보기 (구조: candidates[0].content.parts[0].text)
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text");
                }
            }
            return "현재 시스템 지연으로 응답을 가져오지 못했습니다. 잠시 후 다시 시도해 주십시오.";

        } catch (Exception e) {
            e.printStackTrace();
            return " 다시 한번 물어봐주세요.";
        }
    }
}