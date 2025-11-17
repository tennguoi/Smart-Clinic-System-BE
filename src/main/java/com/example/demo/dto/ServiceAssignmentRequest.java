package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceAssignmentRequest(
        UUID medicalRecordId,
        String serviceId,
        UUID patientId,
        Integer quantity,
        BigDecimal unitPrice,
        String notes
) {}

