// src/main/java/com/example/demo/dto/AddPrescriptionToExaminationRequest.java
package com.example.demo.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddPrescriptionToExaminationRequest(
        @NotNull UUID currentQueueId,
        @NotBlank String drugs,
        @NotBlank String instructions
) {}