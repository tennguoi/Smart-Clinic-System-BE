package com.example.demo.dto.billing;

import com.example.demo.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BillingCreateDTO(
        @NotNull(message = "ID hồ sơ bệnh án không được để trống")
        UUID recordId,

        @NotEmpty(message = "Danh sách dịch vụ không được để trống")
        List<@Valid BillingItemDTO> items,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod
) {}