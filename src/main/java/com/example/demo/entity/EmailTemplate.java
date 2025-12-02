package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "template_type", unique = true, nullable = false, length = 100)
    private String templateType;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_html", nullable = false)
    @Builder.Default
    private Boolean isHtml = true;

    @Column(name = "available_placeholders", columnDefinition = "TEXT")
    private String availablePlaceholders; // JSON string of placeholders

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
