package com.example.demo.dto.prescription;

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
public class PrescriptionResponse {
    private UUID prescriptionId;
    private UUID recordId;
    private String drugs;
    private String instructions;
    private LocalDateTime issuedAt;
}

