package com.example.demo.service;

import com.example.demo.entity.ChatbotConversation;
import com.example.demo.entity.ChatbotMessage;
import com.example.demo.repository.ChatbotConversationRepository;
import com.example.demo.repository.ChatbotMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotConversationRepository convRepo;
    private final ChatbotMessageRepository msgRepo;

    // Tạo hoặc lấy conversation theo sessionId
    public ChatbotConversation getOrCreateConversation(String sessionId) {
        return convRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatbotConversation conv = ChatbotConversation.builder()
                            .sessionId(sessionId)
                            .build();
                    return convRepo.save(conv);
                });
    }

    // Lưu tin nhắn người dùng
    public void saveUserMessage(String sessionId, String message) {
        ChatbotConversation conv = getOrCreateConversation(sessionId);
        ChatbotMessage msg = ChatbotMessage.builder()
                .conversation(conv)
                .sender(ChatbotMessage.SenderType.User)
                .message(message)
                .build();
        msgRepo.save(msg);
    }

    // Lưu tin nhắn bot
    public void saveBotMessage(String sessionId, String message) {
        ChatbotConversation conv = getOrCreateConversation(sessionId);
        ChatbotMessage msg = ChatbotMessage.builder()
                .conversation(conv)
                .sender(ChatbotMessage.SenderType.Bot)
                .message(message)
                .build();
        msgRepo.save(msg);
    }
}