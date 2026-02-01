package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DoctorQueryRequest {
    private String sessionId;
    @NotBlank(message = "Nội dung không được để trống") String text;
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
