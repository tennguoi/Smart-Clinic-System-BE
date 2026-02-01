// src/main/java/com/example/demo/service/TmhAssistantService.java

package com.example.demo.service;

import com.example.demo.entity.ChatbotConversation;
import com.example.demo.entity.ChatbotMessage;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatbotConversationRepository;
import com.example.demo.repository.ChatbotMessageRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TmhAssistantService {

    private final ChatbotConversationRepository conversationRepo;
    private final ChatbotMessageRepository messageRepo;
    private final UserRepository userRepo;

    /**
     * Lấy hoặc tạo cuộc trò chuyện
     * @param sessionId - ID session (mỗi bác sĩ 1 session riêng)
     * @param doctorId - ID bác sĩ đang sử dụng AI
     * @return ChatbotConversation
     */
    public ChatbotConversation getOrCreateConversation(String sessionId, UUID doctorId) {
        log.debug("🔍 Tìm conversation với sessionId: {}", sessionId);

        return conversationRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    log.info("➕ Tạo conversation mới cho sessionId: {}", sessionId);

                    User doctor = doctorId != null
                            ? userRepo.findById(doctorId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ ID: " + doctorId))
                            : null;

                    ChatbotConversation newConv = ChatbotConversation.builder()
                            .sessionId(sessionId)
                            .user(doctor)  // Bác sĩ đang dùng AI
                            .startedAt(LocalDateTime.now())
                            .build();

                    return conversationRepo.save(newConv);
                });
    }

    /**
     * Lưu 1 tin nhắn vào conversation
     * @param conversation - Cuộc trò chuyện
     * @param message - Nội dung tin nhắn
     * @param sender - User (bác sĩ) hoặc Bot (AI)
     */
    public void saveMessage(ChatbotConversation conversation, String message, ChatbotMessage.SenderType sender) {
        log.debug("💬 Lưu tin nhắn từ {}: {}", sender,
                message.length() > 50 ? message.substring(0, 50) + "..." : message);

        ChatbotMessage msg = ChatbotMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepo.save(msg);
    }

    /**
     * Lấy tất cả cuộc trò chuyện của 1 bác sĩ
     * @param doctorId - ID bác sĩ
     * @return Danh sách conversations
     */
    public List<ChatbotConversation> getAllConversationsByDoctor(UUID doctorId) {
        log.debug("📜 Lấy lịch sử cho bác sĩ {}", doctorId);
        return conversationRepo.findAllByUser_UserId(doctorId);
    }

    /**
     * Lấy chi tiết 1 cuộc trò chuyện (kèm tất cả tin nhắn)
     * @param conversationId - ID cuộc trò chuyện
     * @return ChatbotConversation với messages đã load
     */
    public ChatbotConversation getConversationWithMessages(UUID conversationId) {
        log.debug("💬 Lấy chi tiết conversation {}", conversationId);

        return conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện ID: " + conversationId));
    }

    /**
     * Xóa 1 cuộc trò chuyện (kèm tất cả tin nhắn)
     * @param conversationId - ID cuộc trò chuyện cần xóa
     */
    public void deleteConversation(UUID conversationId) {
        log.info("🗑️ Xóa conversation {}", conversationId);

        ChatbotConversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy conversation ID: " + conversationId));

        // Xóa tất cả tin nhắn trước
        if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
            messageRepo.deleteAll(conv.getMessages());
            log.debug("✅ Đã xóa {} tin nhắn", conv.getMessages().size());
        }

        // Sau đó xóa conversation
        conversationRepo.delete(conv);
        log.info("✅ Đã xóa conversation {}", conversationId);
    }

    /**
     * Xóa tất cả cuộc trò chuyện của 1 bác sĩ
     * @param doctorId - ID bác sĩ
     */
    public void deleteAllConversationsByDoctor(UUID doctorId) {
        log.info("🗑️ Xóa tất cả conversation của bác sĩ {}", doctorId);

        List<ChatbotConversation> conversations = conversationRepo.findAllByUser_UserId(doctorId);

        for (ChatbotConversation conv : conversations) {
            if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
                messageRepo.deleteAll(conv.getMessages());
            }
        }

        conversationRepo.deleteAll(conversations);
        log.info("✅ Đã xóa {} cuộc trò chuyện", conversations.size());
    }
}