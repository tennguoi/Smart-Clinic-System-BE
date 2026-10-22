package com.example.demo.dto.invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvoiceItemDTO(
        @NotBlank(message = "Mã dịch vụ không được để trống")
        String serviceId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải ≥ 1")
        Integer quantity
) {}