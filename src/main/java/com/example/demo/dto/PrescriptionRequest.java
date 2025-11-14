package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescriptionRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        
        UUID medicalRecordId, // Optional
        
        @NotNull(message = "Prescribed date is required")
        LocalDateTime prescribedDate,
        
        @Size(max = 1000, message = "Diagnosis cannot exceed 1000 characters")
        String diagnosis,
        
        @Size(max = 2000, message = "Treatment instructions cannot exceed 2000 characters")
        String treatmentInstructions,
        
        @Size(max = 1000, message = "Lifestyle advice cannot exceed 1000 characters")
        String lifestyleAdvice,
        
        LocalDateTime followUpDate,
        
        @Size(max = 1000, message = "Follow up instructions cannot exceed 1000 characters")
        String followUpInstructions,
        
        @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
        String additionalNotes,
        
        String status, // ACTIVE, COMPLETED, CANCELLED
        
        @NotEmpty(message = "At least one medication item is required")
        @Valid
        List<PrescriptionItemRequest> items
) {}
