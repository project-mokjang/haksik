package com.example.haksikmokjang.chatbot.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ChatbotService {



    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked") // 잔소리 그만
    public String getAiResponse(String userMessage) {

        String url = "http://localhost:11434/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gemma2"); //
        requestBody.put("stream", false);   //

        List<Map<String, String>> messages = new ArrayList<>();
        String systemPrompt = "너는 인덕대학교 학생 매칭 앱 '학식목장'의 친절하고 다정한 고객센터 AI 도우미야.\n" +
                "아래의 [학식목장 앱 이용 가이드]를 바탕으로 학생들의 질문에 존댓말로 이모티콘을 섞어서 3줄 이내로 간결하고 정확하게 답변해 줘.\n\n" +
                "[학식목장 앱 이용 가이드]\n" +
                "1. 학식메이트 매칭: 혼자 밥 먹기 싫을 때! '매칭' 메뉴에서 '학식메이트'를 선택하면 지도에서 밥 먹을 사람을 찾을 수 있어.\n" +
                "2. 소개팅 매칭: '매칭' 메뉴에서 '소개팅'을 선택하면 1:1 매칭 상대를 지도에서 확인할 수 있어.\n" +
                "3. 과팅 매칭: '매칭' 메뉴에서 '과팅'을 선택하면 그룹 단위 매칭 상대를 찾을 수 있어.\n" +
                "4. 빠른 메뉴: 화면 하단 네비게이션 바를 통해 [홈, 매칭, 채팅, 커뮤니티, 마이] 탭으로 쉽게 이동할 수 있어.\n" +
                "5. 채팅 확인: 진행 중인 대화는 하단의 '채팅' 메뉴에서 바로 확인할 수 있어.\n" +
                "6. 내 정보 관리: 프로필 수정이나 내 정보 관리는 하단의 '마이' 탭을 이용해 줘.\n\n" +
                "7. 오늘의 음식 추천:  음식 하나 추천해주면 돼.\n" +
                "8. 비매너 신고 : 채팅방에서 오른쪽 상단 점 3개 버튼을 눌러 신고하면 돼.\n"+
                "9. 오늘의 운세: 운세물어보면  얘기해줘 \n" +
                "10.매칭 추천:무슨 매칭를 할지 고민하는 사용자에게 학식 메이트 매칭, 소개팅 매칭, 과팅 매칭 중에 하나를 너가 선택해서 답변해주면 돼.\n"+
                "11.학식목장 앱 이용 가이드 외의 질문은 구글링해서 알려줘\n"+
                "주의사항: 위 가이드에 없는 내용을 물어보면 절대 지어내지 말고, '그 부분은 아직 제가 배우지 못했어요 🥺' 라고 대답해 줘.";

        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Ollama는 포장지가 더 얇아서 한 번만 까면 바로 알맹이가 나와!
            Map<String, String> message = (Map<String, String>) responseBody.get("message");

            return message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return " AI 두뇌에 잠깐 오류가 생겼습니다..잠시 후 다시 시도해 주세요!";
        }
    }
}