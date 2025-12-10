package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String text;

    private Boolean hasImage;
    private String imageBase64;
    private String contentType;

    public Message() {
    }

    public Message(String text) {
        this.text = text;
    }

    public Message(String text, Boolean hasImage, String imageBase64, String contentType) {
        this.text = text;
        this.hasImage = hasImage;
        this.imageBase64 = imageBase64;
        this.contentType = contentType;
    }
}