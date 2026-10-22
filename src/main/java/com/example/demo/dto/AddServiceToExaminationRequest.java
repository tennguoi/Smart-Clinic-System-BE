package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddServiceToExaminationRequest(
        @NotNull(message = "ID hàng đợi hiện tại không được để trống")
        UUID currentQueueId,

        @NotBlank(message = "Mã dịch vụ không được để trống")
        String serviceId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải ≥ 1")
        Integer quantity,

        String note
) {}