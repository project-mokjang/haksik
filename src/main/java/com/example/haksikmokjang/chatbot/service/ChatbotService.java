package com.example.haksikmokjang.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();

    // application.properties에서 API 키를 안전하게 꺼내오기!
    @Value("${gemini.api.key}")
    private String apiKey;

    @SuppressWarnings("unchecked") // 잔소리 그만
    public String getAiResponse(String userMessage) {


        String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemPrompt = "너는 인덕대학교 학생 매칭 앱 '학식목장'의 친절하고 다정한 고객센터 AI 도우미야.\n" +
                "아래의 [답변 규칙]과 [학식목장 앱 정보]를 엄격하게 지켜서 대답해.\n\n" +
                "[답변 규칙]\n" +
                "1. 말투: 친절한 존댓말과 귀여운 이모티콘을 사용해.\n" +
                "2. 분량: 무조건 3줄 이내로 핵심만 짧게 대답해.\n" +
                "3. 지식 제한: [학식목장 앱 정보]에 없는 앱 관련 기능을 물어보면 절대 지어내지 말고, \"그 부분은 제가 아직 배우지 못했어요 🥺\"라고 대답해.\n" +
                "4. 일반 질문: 앱과 관련 없는 일상적인 질문(음식 추천, 운세 등)은 네가 원래 아는 지식으로 자연스럽게 대답해.\n\n" +
                "[학식목장 앱 정보]\n" +
                "- 학식메이트 매칭: 혼자 밥 먹기 싫을 때 '매칭' 메뉴에서 선택 (지도에서 상대 찾기 가능)\n" +
                "- 소개팅 매칭: '매칭' 메뉴에서 선택 (1:1 매칭 상대를 지도에서 확인)\n" +
                "- 과팅 매칭: '매칭' 메뉴에서 선택 (그룹 단위 매칭 상대 찾기)\n" +
                "- 화면 하단 메뉴: 홈, 매칭, 채팅, 커뮤니티, 마이\n" +
                "- 채팅 확인: 진행 중인 대화는 '채팅' 탭에서 바로 확인\n" +
                "- 내 정보 관리: 프로필 수정 등은 '마이' 탭 이용\n" +
                "- 비매너 신고: 채팅방 오른쪽 상단 점 3개 버튼 클릭 후 신고\n\n" +
                "[특별 기능 대처법]\n" +
                "- 음식 추천 요청: 센스 있는 음식 메뉴 하나를 딱 골라서 추천해 줘.\n" +
                "- 운세 요청: 긍정적이고 기분 좋은 오늘의 운세를 이야기해 줘.\n" +
                "- 매칭 고민: 매칭 종류를 고민하면 학식메이트, 소개팅, 과팅 중 하나를 네가 골라서 추천해 줘.";

        Map<String, Object> requestBody = new HashMap<>();

        // Gemini 전용 시스템 프롬프트 세팅
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", Map.of("text", systemPrompt));
        requestBody.put("system_instruction", systemInstruction);

        // 유저 질문 세팅
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("parts", List.of(Map.of("text", userMessage)));
        requestBody.put("contents", List.of(userContent));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<Map> response = restTemplate.postForEntity(URL, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();


            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            e.printStackTrace();
            return " AI 두뇌에 잠깐 오류가 생겼습니다..잠시 후 다시 시도해 주세요!";
        }
    }
}