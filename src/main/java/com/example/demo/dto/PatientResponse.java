package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
        UUID patientId,
        String patientCode,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String identityCard,
        String insuranceNumber,
        String emergencyContact,
        String emergencyPhone,
        String medicalHistory,
        String allergies,
        String notes,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdByName
) {}
