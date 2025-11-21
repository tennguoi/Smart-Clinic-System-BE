// src/main/java/com/example/demo/dto/invoice/InvoiceItemDTO.java
package com.example.demo.dto.invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InvoiceItemDTO(
        @NotBlank(message = "Mã dịch vụ không được để trống")
        String serviceId,

        @Min(value = 1, message = "Số lượng phải ≥ 1")
        Integer quantity
) {}