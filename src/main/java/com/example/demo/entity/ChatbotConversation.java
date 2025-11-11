package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "CHATBOT_CONVERSATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Dùng JPA tạo UUID
    @Column(name = "conversation_id", length = 36)
    private UUID conversationId; // Đổi sang UUID

    // Khóa ngoại đến Appointment (Nếu Entity Appointment tồn tại)
    @Column(name = "appointment_id", length = 36)
    private UUID appointmentId; // Đổi sang UUID

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "started_at")
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<ChatbotMessage> messages;

    // Đã loại bỏ @PrePersist vì việc tạo ID và mặc định thời gian đã được xử lý bằng @GeneratedValue và @Builder.Default
}