package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        
        @NotNull(message = "Examination date is required")
        LocalDateTime examinationDate,
        
        @Size(max = 1000, message = "Chief complaint cannot exceed 1000 characters")
        String chiefComplaint,
        
        @Size(max = 2000, message = "History of present illness cannot exceed 2000 characters")
        String historyOfPresentIllness,
        
        @Size(max = 2000, message = "Physical examination cannot exceed 2000 characters")
        String physicalExamination,
        
        @Size(max = 500, message = "Vital signs cannot exceed 500 characters")
        String vitalSigns,
        
        @Size(max = 1000, message = "Diagnosis cannot exceed 1000 characters")
        String diagnosis,
        
        @Size(max = 1000, message = "Differential diagnosis cannot exceed 1000 characters")
        String differentialDiagnosis,
        
        @Size(max = 2000, message = "Treatment plan cannot exceed 2000 characters")
        String treatmentPlan,
        
        @Size(max = 2000, message = "Medications cannot exceed 2000 characters")
        String medications,
        
        @Size(max = 1000, message = "Follow up instructions cannot exceed 1000 characters")
        String followUpInstructions,
        
        @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
        String additionalNotes,
        
        String status // DRAFT, COMPLETED, ARCHIVED
) {}
