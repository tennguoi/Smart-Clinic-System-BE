package com.example.demo.controller;

import com.example.demo.dto.EmailTemplateResponse;
import com.example.demo.dto.UpdateEmailTemplateRequest;
import com.example.demo.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/email-templates")
@PreAuthorize("hasRole('ADMIN')")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @GetMapping
    public ResponseEntity<List<EmailTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(emailTemplateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> getTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(emailTemplateService.getTemplateById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> updateTemplate(
            @PathVariable UUID id,
            @RequestBody UpdateEmailTemplateRequest request) {
        return ResponseEntity.ok(emailTemplateService.updateTemplate(id, request));
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<EmailTemplateResponse> resetToDefault(@PathVariable UUID id) {
        return ResponseEntity.ok(emailTemplateService.resetToDefault(id));
    }
}
