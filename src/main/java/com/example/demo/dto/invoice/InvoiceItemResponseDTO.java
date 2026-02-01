// src/main/java/com/example/demo/dto/invoice/InvoiceItemResponseDTO.java
package com.example.demo.dto.invoice;

import java.math.BigDecimal;

public record InvoiceItemResponseDTO(
        String serviceName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {}