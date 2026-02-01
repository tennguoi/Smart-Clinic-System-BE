// src/main/java/com/example/demo/dto/invoice/InvoiceResponseDTO.java
package com.example.demo.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponseDTO(
        UUID invoiceId,
        String invoiceCode,
        String patientName,
        String patientPhone,
        LocalDateTime createdAt,
        String createdByName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String status,
        List<InvoiceItemResponseDTO> items
) {}