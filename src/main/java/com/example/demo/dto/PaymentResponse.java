package com.example.demo.dto;

import com.example.demo.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID patientId,
        String patientName,
        UUID queueId,
        String queueNumber,
        UUID processedById,
        String processedByName,
        String paymentCode,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        Payment.PaymentMethod paymentMethod,
        Payment.Status status,
        LocalDateTime paymentDate,
        String notes,
        LocalDateTime createdAt
) {}

