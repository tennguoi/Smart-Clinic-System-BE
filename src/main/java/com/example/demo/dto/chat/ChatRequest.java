// ChatRequest.java
package com.example.demo.dto.chat;

import com.example.demo.dto.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ChatRequest {

    @NotBlank(message = "sessionId là bắt buộc")
    private final String sessionId;

    // Có thể null – chỉ dùng khi bên thứ 3 đã quản lý conversationId riêng
    private final String conversationId;

    // UUID của bệnh nhân trong hệ thống bác sĩ (NULL = khách vãng lai)
    private final UUID userId;

    @NotNull(message = "message không được null")
    @Valid
    private final Message message;

    // Constructor chính (dùng khi từ Zalo/FB)
    public ChatRequest(String sessionId, String conversationId, UUID userId, Message message) {
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.userId = userId;
        this.message = message;
    }

    // Constructor tiện lợi khi chưa có userId (khách vãng lai)
    public ChatRequest(String sessionId, Message message) {
        this(sessionId, null, null, message);
    }

    // Constructor khi đã có userId (người dùng đăng nhập trên web/app)
    public ChatRequest(String sessionId, UUID userId, Message message) {
        this(sessionId, null, userId, message);
    }
}
