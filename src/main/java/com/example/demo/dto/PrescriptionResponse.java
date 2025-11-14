package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
        UUID prescriptionId,
        String prescriptionCode,
        UUID patientId,
        String patientName,
        String patientCode,
        UUID doctorId,
        String doctorName,
        UUID medicalRecordId,
        LocalDateTime prescribedDate,
        String diagnosis,
        String treatmentInstructions,
        String lifestyleAdvice,
        LocalDateTime followUpDate,
        String followUpInstructions,
        String additionalNotes,
        String status,
        List<PrescriptionItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
