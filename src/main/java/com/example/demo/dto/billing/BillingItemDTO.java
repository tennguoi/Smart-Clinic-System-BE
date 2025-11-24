// src/main/java/com/example/demo/dto/billing/BillingItemDTO.java
package com.example.demo.dto.billing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BillingItemDTO(
        @NotBlank String serviceId,
        @Min(1) Integer quantity,
        String description
) {}