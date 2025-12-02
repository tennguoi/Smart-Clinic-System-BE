package com.example.demo.dto;

import lombok.Data;

@Data
public class UpdateEmailTemplateRequest {
    private String subject;
    private String content;
    private Boolean isHtml;
}
