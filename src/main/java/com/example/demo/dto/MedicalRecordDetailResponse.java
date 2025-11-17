package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDetailResponse {
    private UUID recordId;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private String diagnosis;
    private String treatmentNotes;
    private LocalDateTime createdAt;
    private PrescriptionResponse prescription; // Đơn thuốc (có thể null nếu chưa có)
}

