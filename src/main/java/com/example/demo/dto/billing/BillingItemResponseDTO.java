// src/main/java/com/example/demo/dto/billing/BillingItemResponseDTO.java
package com.example.demo.dto.billing;

import java.math.BigDecimal;

public record BillingItemResponseDTO(
        String serviceName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal,
        String description
) {}