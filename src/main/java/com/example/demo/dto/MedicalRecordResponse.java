package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordResponse(
        UUID recordId,
        UUID patientId,
        String patientName,
        String patientCode,
        UUID doctorId,
        String doctorName,
        LocalDateTime examinationDate,
        String chiefComplaint,
        String historyOfPresentIllness,
        String physicalExamination,
        String vitalSigns,
        String diagnosis,
        String differentialDiagnosis,
        String treatmentPlan,
        String medications,
        String followUpInstructions,
        String additionalNotes,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
