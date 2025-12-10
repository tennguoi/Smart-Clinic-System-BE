package com.example.demo.controller.chatbot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.chat.ChatRequest;
import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.entity.ChatbotConversation;
import com.example.demo.service.ChatbotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final RestTemplate restTemplate;

    @PostMapping("/webhook/chat")
    public ResponseEntity<ChatResponse> handleChat(@Valid @RequestBody ChatRequest request) {
        String sessionId = request.getSessionId();
        String userMessage = request.getMessage().getText();
        boolean hasImage = Boolean.TRUE.equals(request.getMessage().getHasImage());

        log.info("📨 Chat request - SessionID: {}, HasImage: {}", sessionId, hasImage);

        // Lưu tin người dùng
        ChatbotConversation conv = chatbotService.getOrCreateConversation(sessionId);

        // Nếu có ảnh, thêm emoji vào message
        String messageToSave = hasImage ? "📷 " + userMessage : userMessage;
        chatbotService.saveUserMessage(sessionId, messageToSave);

        // Gọi n8n AI Agent
        String botResponse = callN8nAgent(request);

        // Lưu tin bot
        chatbotService.saveBotMessage(sessionId, botResponse);

        return ResponseEntity.ok(
                new ChatResponse(botResponse, conv.getConversationId().toString())
        );
    }

    private String callN8nAgent(ChatRequest request) {
        try {
            // Tạo payload gửi đến n8n
            Map<String, Object> body = new HashMap<>();
            body.put("sessionId", request.getSessionId());
            body.put("message", Map.of("text", request.getMessage().getText()));

            // Nếu có ảnh, gửi kèm base64
            if (Boolean.TRUE.equals(request.getMessage().getHasImage())) {
                body.put("imageBase64", request.getMessage().getImageBase64());
                body.put("contentType", request.getMessage().getContentType());
                body.put("hasImage", true);
                log.info("📷 Sending image to n8n - ContentType: {}",
                        request.getMessage().getContentType());
            }

            log.info("🔄 Calling n8n webhook...");

            ResponseEntity<Map> res = restTemplate.postForEntity(
                    "https://n8n.quanliduan-pms.site/webhook/chat-ocr",
                    body,
                    Map.class
            );

            String response = (String) res.getBody().get("text");
            log.info("✅ n8n response received");

            return response;

        } catch (Exception e) {
            log.error("❌ Error calling n8n agent", e);
            return "Xin lỗi, em đang gặp sự cố. Vui lòng thử lại sau!";
        }
    }
}