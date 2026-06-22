package com.example.haksikmokjang.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ChatbotService {


    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @SuppressWarnings("unchecked")
    public String getAiResponse(String userMessage, String preferredFood) {


        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 선호 음식 세팅
        String foodInstruction = "";
        if (preferredFood != null && !preferredFood.isEmpty()) {
            foodInstruction = "이 사용자는 현재 [" + preferredFood + "]을(를) 가장 좋아해! 만약 사용자가 음식 추천을 해달라고 하면, 반드시 이 [" + preferredFood + "] 카테고리 안에서 센스 있게 딱 1개의 메뉴만 골라서 추천해 줘!\n";
        } else {
            foodInstruction = "이 사용자는 아직 선호 음식을 설정하지 않았어. 음식 추천을 해달라고 하면, 한식, 중식, 일식, 양식 중에서 랜덤으로 맛있어 보이는 메뉴 1개를 골라 추천해 줘!\n";
        }

        String systemPrompt = "너는 인덕대학교 학생 매칭 앱 '학식목장'의 친절하고 다정한 고객센터 AI 도우미야.\n" +
                "아래의 [학식목장 앱 이용 가이드]를 바탕으로 학생들의 질문에 존댓말로 이모티콘을 섞어서 3줄 이내로 간결하고 정확하게 답변해 줘.\n\n" +
                "[학식목장 앱 이용 가이드]\n" +
                "1. 학식메이트 매칭: 혼자 밥 먹기 싫을 때! '매칭' 메뉴에서 '학식메이트'를 선택하면 지도에서 밥 먹을 사람을 찾을 수 있어.\n" +
                "2. 소개팅 매칭: '매칭' 메뉴에서 '소개팅'을 선택하면 1:1 매칭 상대를 지도에서 확인할 수 있어.\n" +
                "3. 과팅 매칭: '매칭' 메뉴에서 '과팅'을 선택하면 그룹 단위 매칭 상대를 찾을 수 있어.\n" +
                "4. 빠른 메뉴: 화면 하단 네비게이션 바를 통해 [홈, 매칭, 채팅, 커뮤니티, 마이] 탭으로 쉽게 이동할 수 있어.\n" +
                "5. 채팅 확인: 진행 중인 대화는 하단의 '채팅' 메뉴에서 바로 확인할 수 있어.\n" +
                "6. 내 정보 관리: 프로필 수정이나 내 정보 관리는 하단의 '마이' 탭을 이용해 줘.\n\n" +
                "7. 오늘의 음식 추천: " + foodInstruction +
                "8. 비매너 신고 : 채팅방에서 오른쪽 상단 점 3개 버튼을 눌러 신고하면 돼.\n" +
                "9. 오늘의 운세: 운세물어보면 얘기해줘 \n" +
                "10.매칭 추천:무슨 매칭를 할지 고민하는 사용자에게 학식 메이트 매칭, 소개팅 매칭, 과팅 매칭 중에 하나를 너가 선택해서 답변해주면 돼.\n" +
                "11.학식목장 앱 이용 가이드 외의 질문은 구글링해서 알려줘\n" +
                "주의사항: 위 가이드에 없는 내용을 물어보면 절대 지어내지 말고, '그 부분은 아직 제가 배우지 못했어요 🥺' 라고 대답해 줘.";

        // 제미나이 입맛에 맞게 JSON 바디 포장하기!
        Map<String, Object> requestBody = new HashMap<>();

        // 시스템 프롬프트 넣기
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


            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text");
                }
            }
            return "제미나이가 응답을 주지 않음";

        } catch (Exception e) {
            e.printStackTrace();
            return "  두뇌에 잠깐 오류가 생겼습니다.. ";
        }
    }
}