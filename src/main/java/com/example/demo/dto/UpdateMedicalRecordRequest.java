package com.example.demo.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateMedicalRecordRequest {
    private UUID patientId; // optional
    private String patientName;
    private String diagnosis;
    private String treatmentNotes;
    
    // Thông tin đơn thuốc (có thể null nếu không muốn cập nhật)
    private PrescriptionUpdateRequest prescription;
    
    @Data
    public static class PrescriptionUpdateRequest {
        private String drugs;
        private String instructions;
    }
}

