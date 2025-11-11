// ChatRequest.java
package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatRequest {
    @NotBlank(message = "Session ID không được để trống")
    private final String sessionId;

    private final String conversationId;

    @NotNull(message = "Message không được null")
    @Valid  // ⭐ Kích hoạt validation cho nested object
    private final Message message;

    public ChatRequest(String sessionId, String conversationId, Message message) {
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.message = message;
    }

    public String getSessionId() { return sessionId; }
    public String getConversationId() { return conversationId; }
    public Message getMessage() { return message; }
}