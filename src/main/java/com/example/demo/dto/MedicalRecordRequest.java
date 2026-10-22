package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class MedicalRecordRequest {
    private UUID patientId; // optional
    private String patientName;

    @NotBlank(message = "Chẩn đoán bệnh không được để trống")
    private String diagnosis;

    private String treatmentNotes;
}
