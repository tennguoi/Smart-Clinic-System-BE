package com.example.demo.dto.billing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BillingItemDTO(
        @NotBlank(message = "Mã dịch vụ không được để trống")
        String serviceId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng dịch vụ phải ≥ 1")
        Integer quantity,

        @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
        String description
) {}