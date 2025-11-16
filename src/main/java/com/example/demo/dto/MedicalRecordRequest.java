package com.example.demo.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MedicalRecordRequest {
    private UUID patientId; // optional
    private String diagnosis;
    private String treatmentNotes;
}


