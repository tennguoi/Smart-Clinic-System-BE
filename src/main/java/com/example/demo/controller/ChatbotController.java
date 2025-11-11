package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.entity.ChatbotConversation;
import com.example.demo.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/webhook/chat")
    public ResponseEntity<ChatResponse> handleChat(@Valid @RequestBody ChatRequest request) {
        String sessionId = request.getSessionId();
        String userMessage = request.getMessage().getText();

        // Lưu tin người dùng
        ChatbotConversation conv = chatbotService.getOrCreateConversation(sessionId);
        chatbotService.saveUserMessage(sessionId, userMessage);

        // Gọi n8n AI Agent
        String botResponse = callN8nAgent(request);

        // Lưu tin bot
        chatbotService.saveBotMessage(sessionId, botResponse);

        return ResponseEntity.ok(new ChatResponse(botResponse, conv.getConversationId().toString()));
    }

    private String callN8nAgent(ChatRequest request) {
        RestTemplate rest = new RestTemplate();
        try {
            Map<String, Object> body = Map.of(
                    "sessionId", request.getSessionId(),
                    "message", Map.of("text", request.getMessage().getText())
            );

            ResponseEntity<Map> res = rest.postForEntity(
                    "https://n8n.quanliduan-pms.site/webhook/chat",
                    body,
                    Map.class
            );
            return (String) res.getBody().get("output");
        } catch (Exception e) {
            return "Xin lỗi, em đang gặp sự cố. Vui lòng thử lại sau!";
        }
    }
}

