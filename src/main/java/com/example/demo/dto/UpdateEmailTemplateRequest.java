package com.example.demo.dto;

import lombok.Data;

@Data
public class UpdateEmailTemplateRequest {
    private String subject;
    private String content;
    private String bodyContent; // Editable body content from React Quill
    private Boolean isHtml;
}
