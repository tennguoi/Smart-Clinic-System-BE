package com.example.demo.dto.chat;

import com.example.demo.dto.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class ChatRequest {

    @NotBlank(message = "sessionId là bắt buộc")
    String sessionId;

    String conversationId;

    UUID userId;

    @NotNull(message = "message không được null")
    @Valid
    Message message;
}