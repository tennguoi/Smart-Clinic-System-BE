// src/main/java/com/example/demo/dto/AddServiceToExaminationRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AddServiceToExaminationRequest(
        @NotNull UUID currentQueueId,  // queueId của bệnh nhân đang khám
        @NotBlank String serviceId,
        @Min(1) Integer quantity,
        String note
) {}