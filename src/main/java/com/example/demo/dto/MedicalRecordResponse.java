package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MedicalRecordResponse {
    private UUID recordId;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private String diagnosis;
    private String treatmentNotes;
    private LocalDateTime createdAt;
}