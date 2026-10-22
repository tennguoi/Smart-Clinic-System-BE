package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceRequest(
        @NotBlank(message = "Tên dịch vụ không được để trống")
        @Size(max = 255, message = "Tên dịch vụ không được vượt quá 255 ký tự")
        String name,

        String description,

        String category,

        @NotNull(message = "Giá dịch vụ không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá dịch vụ phải lớn hơn 0")
        BigDecimal price,

        Boolean isActive,

        String photoUrl
) {}
