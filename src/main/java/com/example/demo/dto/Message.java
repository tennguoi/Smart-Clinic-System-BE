// Message.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class Message {
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private final String text;

    public Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}