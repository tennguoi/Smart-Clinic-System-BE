package com.example.demo.controller.chatbot;

import com.example.demo.entity.ChatbotConversation;
import com.example.demo.entity.ChatbotMessage;
import com.example.demo.entity.ChatbotMessage.SenderType;
import com.example.demo.service.TmhAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/tmh-assistant")
@PreAuthorize("hasAnyRole('BAC_SI', 'ADMIN')")
public class TmhAssistantController {

    private final TmhAssistantService tmhService;

    // ========================================
    // 1. TẠO CUỘC TRÒ CHUYỆN MỚI
    // ========================================
    @PostMapping("/new-conversation")
    public ResponseEntity<Map<String, Object>> createNewConversation(
            @RequestHeader("X-Doctor-Id") UUID doctorId) {

        try {
            String newSessionId = "DOCTOR_" + doctorId + "_" + UUID.randomUUID();

            log.info("🆕 Tạo cuộc trò chuyện mới cho bác sĩ {}", doctorId);

            ChatbotConversation conv = tmhService.getOrCreateConversation(newSessionId, doctorId);

            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conv.getConversationId().toString());
            response.put("sessionId", newSessionId);
            response.put("startedAt", conv.getStartedAt().toString());
            response.put("message", "Tạo cuộc trò chuyện mới thành công");

            log.info("✅ Tạo thành công sessionId: {}", newSessionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo cuộc trò chuyện", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Không thể tạo cuộc trò chuyện: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========================================
    // 2. LƯU TIN NHẮN SAU KHI FRONTEND ĐÃ GỌI n8n
    // ========================================
    @PostMapping("/save-message")
    public ResponseEntity<Map<String, Object>> saveMessage(
            @RequestHeader("X-Doctor-Id") UUID doctorId,
            @RequestBody Map<String, String> payload) {

        String sessionId = payload.get("sessionId");
        String userMessage = payload.get("userMessage");
        String aiMessage = payload.get("aiMessage");

        log.info("💾 Lưu tin nhắn - sessionId: {}, doctorId: {}", sessionId, doctorId);

        if (sessionId == null || sessionId.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Thiếu sessionId");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            ChatbotConversation conv = tmhService.getOrCreateConversation(sessionId, doctorId);

            // Lưu tin nhắn của bác sĩ
            if (userMessage != null && !userMessage.isBlank()) {
                tmhService.saveMessage(conv, userMessage, SenderType.User);
                log.debug("✅ Đã lưu tin nhắn bác sĩ");
            }

            // Lưu tin nhắn của AI
            if (aiMessage != null && !aiMessage.isBlank()) {
                tmhService.saveMessage(conv, aiMessage, SenderType.Bot);
                log.debug("✅ Đã lưu tin nhắn AI");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conv.getConversationId().toString());
            response.put("saved", true);
            response.put("messageCount", conv.getMessages() != null ? conv.getMessages().size() : 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lưu tin nhắn", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lưu tin nhắn thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========================================
    // 3. LẤY LỊCH SỬ TẤT CẢ CUỘC TRÒ CHUYỆN CỦA BÁC SĨ
    // ========================================
    @GetMapping("/history")
    public ResponseEntity<?> getDoctorHistory(@RequestHeader("X-Doctor-Id") UUID doctorId) {
        try {
            log.info("📜 Lấy lịch sử bác sĩ ID: {}", doctorId);

            List<ChatbotConversation> conversations = tmhService.getAllConversationsByDoctor(doctorId);

            List<Map<String, Object>> response = new ArrayList<>();

            for (ChatbotConversation conv : conversations) {
                Map<String, Object> convMap = new HashMap<>();
                convMap.put("conversationId", conv.getConversationId().toString());
                convMap.put("sessionId", conv.getSessionId());
                convMap.put("startedAt", conv.getStartedAt().toString());

                int messageCount = (conv.getMessages() != null) ? conv.getMessages().size() : 0;
                convMap.put("messageCount", messageCount);

                String firstMessage = "Cuộc trò chuyện mới";
                if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
                    firstMessage = conv.getMessages().get(0).getMessage();
                    if (firstMessage.length() > 80) {
                        firstMessage = firstMessage.substring(0, 80) + "...";
                    }
                }
                convMap.put("firstMessage", firstMessage);

                response.add(convMap);
            }

            log.info("✅ Tìm thấy {} cuộc trò chuyện", response.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy lịch sử bác sĩ {}", doctorId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Không thể lấy lịch sử: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========================================
    // 4. LẤY CHI TIẾT 1 CUỘC TRÒ CHUYỆN (KÈM TIN NHẮN)
    // ========================================
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationDetail(
            @PathVariable UUID conversationId,
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        try {
            log.info("💬 Lấy chi tiết conversation: {}", conversationId);

            ChatbotConversation conversation = tmhService.getConversationWithMessages(conversationId);

            // Kiểm tra quyền truy cập
            if (conversation.getUser() != null &&
                    !conversation.getUser().getUserId().equals(doctorId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Bạn không có quyền xem cuộc trò chuyện này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Convert messages sang DTO
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

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversation.getConversationId().toString());
            response.put("sessionId", conversation.getSessionId());
            response.put("startedAt", conversation.getStartedAt().toString());
            response.put("messages", messages);

            log.info("✅ Trả về {} tin nhắn", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy conversation {}", conversationId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Không thể lấy chi tiết: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========================================
    // 5. XÓA 1 CUỘC TRÒ CHUYỆN (BONUS)
    // ========================================
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            @PathVariable UUID conversationId,
            @RequestHeader("X-Doctor-Id") UUID doctorId) {
        try {
            log.info("🗑️ Xóa conversation: {}", conversationId);

            ChatbotConversation conv = tmhService.getConversationWithMessages(conversationId);

            // Kiểm tra quyền
            if (conv.getUser() != null && !conv.getUser().getUserId().equals(doctorId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Bạn không có quyền xóa cuộc trò chuyện này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            tmhService.deleteConversation(conversationId);

            Map<String, Object> response = new HashMap<>();
            response.put("deleted", true);
            response.put("conversationId", conversationId.toString());

            log.info("✅ Đã xóa conversation {}", conversationId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi xóa conversation {}", conversationId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Không thể xóa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}