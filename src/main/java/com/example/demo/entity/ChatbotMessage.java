package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CHATBOT_MESSAGES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Dùng JPA tạo UUID
    @Column(name = "message_id", length = 36)
    private UUID messageId; // Đổi sang UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatbotConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private SenderType sender;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "sent_at")
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    public enum SenderType {
        User, Bot
    }
}