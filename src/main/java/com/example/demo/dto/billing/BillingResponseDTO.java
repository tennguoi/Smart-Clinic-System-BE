// src/main/java/com/example/demo/dto/billing/BillingResponseDTO.java
package com.example.demo.dto.billing;

import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BillingResponseDTO(
        UUID billId,
        String patientName,
        String patientPhone,
        LocalDateTime createdAt,
        String receptionistName,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        BigDecimal amountPaid,
        BigDecimal remainingAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        LocalDateTime paidAt,
        List<BillingItemResponseDTO> items
) {}