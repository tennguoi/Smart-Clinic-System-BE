// src/main/java/com/example/demo/service/TmhAssistantService.java
package com.example.demo.service;

import com.example.demo.entity.ChatbotConversation;
import com.example.demo.entity.ChatbotMessage;
import com.example.demo.entity.User; // Entity User là bác sĩ / nhân viên y tế
import com.example.demo.repository.ChatbotConversationRepository;
import com.example.demo.repository.ChatbotMessageRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TmhAssistantService {

    private final ChatbotConversationRepository conversationRepo;
    private final ChatbotMessageRepository messageRepo;
    private final UserRepository userRepo;

    /**
     * Lấy hoặc tạo conversation cho BÁC SĨ (userId là ID bác sĩ đang đăng nhập)
     * sessionId = Zalo sender.id (bệnh nhân)
     * user_id trong DB = bác sĩ đang dùng trợ lý
     */
    public ChatbotConversation getOrCreateConversation(String sessionId, UUID doctorId) {
        // Ưu tiên tìm theo sessionId (mỗi bệnh nhân 1 session)
        return conversationRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    User doctor = doctorId != null
                            ? userRepo.findById(doctorId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ ID: " + doctorId))
                            : null;

                    ChatbotConversation newConv = ChatbotConversation.builder()
                            .sessionId(sessionId)           // ID bệnh nhân từ Zalo
                            .user(doctor)                   // ← ĐÂY LÀ BÁC SĨ đang dùng AI
                            .startedAt(LocalDateTime.now())
                            .build();
                    return conversationRepo.save(newConv);
                });
    }

    public void saveMessage(ChatbotConversation conversation, String message, ChatbotMessage.SenderType sender) {
        ChatbotMessage msg = ChatbotMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepo.save(msg);
    }

    // Bonus: Lấy toàn bộ lịch sử chat của bác sĩ này
    public List<ChatbotConversation> getAllConversationsByDoctor(UUID doctorId) {
        return conversationRepo.findAllByUser_UserId(doctorId);
    }

    // Bonus: Lấy chi tiết 1 cuộc trò chuyện (kèm tin nhắn)
    public ChatbotConversation getConversationWithMessages(UUID conversationId) {
        return conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));
    }
}