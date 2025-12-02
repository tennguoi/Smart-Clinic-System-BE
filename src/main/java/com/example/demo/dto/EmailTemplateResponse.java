package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {
    private UUID templateId;
    private String templateType;
    private String templateName;
    private String subject;
    private String content;
    private Boolean isHtml;
    private List<String> availablePlaceholders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
