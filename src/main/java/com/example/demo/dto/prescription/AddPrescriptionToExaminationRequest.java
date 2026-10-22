package com.example.demo.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddPrescriptionToExaminationRequest(
        @NotNull(message = "ID hàng đợi hiện tại không được để trống")
        UUID currentQueueId,

        @NotBlank(message = "Danh sách thuốc không được để trống")
        String drugs,

        @NotBlank(message = "Hướng dẫn sử dụng thuốc không được để trống")
        String instructions
) {}