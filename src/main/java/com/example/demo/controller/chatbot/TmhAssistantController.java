// TmhAssistantController.java - HOÀN CHỈNH

package com.example.demo.controller.chatbot;

import com.example.demo.dto.chat.ChatResponse;
import com.example.demo.dto.chat.DoctorQueryRequest;
import com.example.demo.entity.ChatbotConversation;
import com.example.demo.entity.ChatbotMessage;
import com.example.demo.entity.ChatbotMessage.SenderType;
import com.example.demo.service.TmhAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/tmh-assistant")
public class TmhAssistantController {

    private final TmhAssistantService tmhService;
    private final RestTemplate restTemplate;

    // n8n webhook URL - ĐỔI THÀNH URL ĐÚNG CỦA BẠN
    private static final String N8N_TMH_WEBHOOK = "https://n8n.quanliduan-pms.site/webhook/ai-support";

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askAssistant(
            @RequestHeader("X-Doctor-Id") UUID doctorId,
            @Valid @RequestBody DoctorQueryRequest request) {

        String doctorMessage = request.getText();
        String sessionId = "DOCTOR_" + doctorId.toString();

        log.info("👨‍⚕️ Bác sĩ {} hỏi: {}", doctorId, doctorMessage);

        try {
            ChatbotConversation conversation = tmhService.getOrCreateConversation(sessionId, doctorId);
            tmhService.saveMessage(conversation, doctorMessage, SenderType.User);
            String aiResponse = callTmhN8nAgent(sessionId, doctorMessage);
            tmhService.saveMessage(conversation, aiResponse, SenderType.Bot);

            log.info("✅ AI trả lời và đã lưu DB: {}",
                    aiResponse.substring(0, Math.min(100, aiResponse.length())));

            return ResponseEntity.ok(
                    new ChatResponse(aiResponse, conversation.getConversationId().toString())
            );

        } catch (Exception e) {
            log.error("❌ Lỗi trợ lý AI - Bác sĩ ID: {}", doctorId, e);
            String errorMsg = "AI đang bận, vui lòng thử lại sau ít phút.";

            try {
                ChatbotConversation conv = tmhService.getOrCreateConversation(sessionId, doctorId);
                tmhService.saveMessage(conv, errorMsg, SenderType.Bot);
            } catch (Exception ex) {
                log.error("Không lưu được lỗi", ex);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse(errorMsg, null));
        }
    }

    /**
     * Lấy lịch sử hỏi đáp của bác sĩ (danh sách conversations)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getDoctorHistory(@RequestHeader("X-Doctor-Id") UUID doctorId) {
        try {
            log.info("📜 Lấy lịch sử bác sĩ ID: {}", doctorId);

            List<ChatbotConversation> conversations = tmhService.getAllConversationsByDoctor(doctorId);

            // Convert sang DTO để trả về frontend - SỬA LỖI TYPE
            List<Map<String, Object>> response = new ArrayList<>();

            for (ChatbotConversation conv : conversations) {
                Map<String, Object> convMap = new HashMap<>();
                convMap.put("conversationId", conv.getConversationId().toString());
                convMap.put("sessionId", conv.getSessionId());
                convMap.put("startedAt", conv.getStartedAt().toString());

                // Đếm số tin nhắn
                int messageCount = (conv.getMessages() != null) ? conv.getMessages().size() : 0;
                convMap.put("messageCount", messageCount);

                // Lấy tin nhắn đầu tiên
                String firstMessage = "Cuộc trò chuyện mới";
                if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
                    firstMessage = conv.getMessages().get(0).getMessage();
                }
                convMap.put("firstMessage", firstMessage);

                response.add(convMap);
            }

            log.info("✅ Tìm thấy {} cuộc trò chuyện", response.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy lịch sử bác sĩ {}", doctorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy lịch sử: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết 1 cuộc trò chuyện (kèm toàn bộ tin nhắn)
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationDetail(
            @PathVariable UUID conversationId,
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        try {
            log.info("💬 Lấy chi tiết conversation: {}", conversationId);

            ChatbotConversation conversation = tmhService.getConversationWithMessages(conversationId);

            // Kiểm tra bác sĩ có quyền xem không
            if (conversation.getUser() != null &&
                    !conversation.getUser().getUserId().equals(doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền xem cuộc trò chuyện này"));
            }

            // Convert messages sang DTO - SỬA LỖI TYPE
            List<Map<String, Object>> messages = new ArrayList<>();

            if (conversation.getMessages() != null) {
                for (ChatbotMessage msg : conversation.getMessages()) {
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("messageId", msg.getMessageId().toString());
                    msgMap.put("sender", msg.getSender().toString());
                    msgMap.put("message", msg.getMessage());
                    msgMap.put("sentAt", msg.getSentAt().toString());
                    messages.add(msgMap);
                }
            }

            // Tạo response object
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversation.getConversationId().toString());
            response.put("sessionId", conversation.getSessionId());
            response.put("startedAt", conversation.getStartedAt().toString());
            response.put("messages", messages);

            log.info("✅ Trả về {} tin nhắn", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy conversation {}", conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lấy chi tiết: " + e.getMessage()));
        }
    }

    private String callTmhN8nAgent(String sessionId, String message) {
        RestTemplate rest = new RestTemplate();

        try {
            Map<String, Object> body = Map.of(
                    "sessionId", sessionId,
                    "chatInput", message
            );

            log.debug("🚀 Gọi n8n với payload: {}", body);

            // Thử parse JSON trước
            try {
                ResponseEntity<Map> res = rest.postForEntity(N8N_TMH_WEBHOOK, body, Map.class);
                Map<String, Object> responseBody = res.getBody();

                if (responseBody != null && responseBody.containsKey("output")) {
                    return (String) responseBody.get("output");
                } else if (responseBody != null && responseBody.containsKey("text")) {
                    return (String) responseBody.get("text");
                } else {
                    log.warn("n8n response không có field 'output' hoặc 'text': {}", responseBody);
                    return "AI trả lời không đúng định dạng.";
                }

            } catch (Exception jsonEx) {
                // Nếu không parse được JSON, thử plain text
                log.debug("Không parse được JSON, thử plain text...");
                ResponseEntity<String> res = rest.postForEntity(N8N_TMH_WEBHOOK, body, String.class);
                String plainText = res.getBody();
                return plainText != null ? plainText.trim() : "AI không phản hồi.";
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ n8n HTTP Error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI gặp lỗi kỹ thuật. Vui lòng liên hệ admin.";
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("❌ Không kết nối được n8n: {}", e.getMessage());
            return "Không kết nối được AI. Vui lòng kiểm tra mạng.";
        } catch (Exception e) {
            log.error("❌ Lỗi gọi n8n AI TMH", e);
            return "AI tạm thời không phản hồi. Vui lòng thử lại.";
        }
    }
}