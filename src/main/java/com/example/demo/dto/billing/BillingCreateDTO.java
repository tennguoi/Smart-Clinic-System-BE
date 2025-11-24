// src/main/java/com/example/demo/dto/billing/BillingCreateDTO.java
package com.example.demo.dto.billing;

import com.example.demo.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BillingCreateDTO(
        @NotNull UUID recordId,
        @NotEmpty List<BillingItemDTO> items,
        @NotNull PaymentMethod paymentMethod
) {}