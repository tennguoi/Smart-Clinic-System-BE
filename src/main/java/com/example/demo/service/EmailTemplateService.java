package com.example.demo.service;

import com.example.demo.dto.EmailTemplateResponse;
import com.example.demo.dto.UpdateEmailTemplateRequest;
import com.example.demo.entity.EmailTemplate;
import com.example.demo.repository.EmailTemplateRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Get all email templates
     */
    public List<EmailTemplateResponse> getAllTemplates() {
        return emailTemplateRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get template by ID
     */
    public EmailTemplateResponse getTemplateById(UUID id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        return convertToResponse(template);
    }

    /**
     * Get template by type
     */
    public EmailTemplate getTemplateByType(String templateType) {
        return emailTemplateRepository.findByTemplateType(templateType)
                .orElseThrow(() -> new RuntimeException("Template not found with type: " + templateType));
    }

    /**
     * Update template
     */
    public EmailTemplateResponse updateTemplate(UUID id, UpdateEmailTemplateRequest request) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        if (request.getSubject() != null) {
            template.setSubject(request.getSubject());
        }
        if (request.getContent() != null) {
            template.setContent(request.getContent());
        }
        if (request.getIsHtml() != null) {
            template.setIsHtml(request.getIsHtml());
        }

        EmailTemplate saved = emailTemplateRepository.save(template);
        return convertToResponse(saved);
    }

    /**
     * Reset template to default
     */
    public EmailTemplateResponse resetToDefault(UUID id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        // Get default template content based on type
        Map<String, String> defaults = EmailTemplateDefaults.getDefault(template.getTemplateType());

        template.setSubject(defaults.get("subject"));
        template.setContent(defaults.get("content"));
        template.setIsHtml(true);

        EmailTemplate saved = emailTemplateRepository.save(template);
        return convertToResponse(saved);
    }

    /**
     * Replace placeholders in template content
     */
    public String replacePlaceholders(String content, Map<String, String> placeholders) {
        if (content == null || placeholders == null) {
            return content;
        }

        String result = content;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * Convert entity to response DTO
     */
    private EmailTemplateResponse convertToResponse(EmailTemplate template) {
        List<String> placeholders = null;
        try {
            if (template.getAvailablePlaceholders() != null) {
                placeholders = objectMapper.readValue(
                        template.getAvailablePlaceholders(),
                        new TypeReference<List<String>>() {
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return EmailTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .templateType(template.getTemplateType())
                .templateName(template.getTemplateName())
                .subject(template.getSubject())
                .content(template.getContent())
                .isHtml(template.getIsHtml())
                .availablePlaceholders(placeholders)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
