package com.example.demo.dto;

import com.example.demo.entity.Payment;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID patientId,
        UUID queueId,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        Payment.PaymentMethod paymentMethod,
        String notes
) {}

