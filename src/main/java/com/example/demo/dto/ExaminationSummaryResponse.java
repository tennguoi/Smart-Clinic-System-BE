// src/main/java/com/example/demo/dto/ExaminationSummaryResponse.java
package com.example.demo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExaminationSummaryResponse(
        UUID recordId,
        UUID patientId,
        String patientName,
        String diagnosis,
        String treatmentNotes,
        LocalDateTime createdAt,
        PrescriptionResponse prescription,
        List<ExaminationServiceItem> services,
        BigDecimal totalServiceAmount
) {
    public record ExaminationServiceItem(
            String serviceId,
            String serviceName,
            String category,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String note
    ) {}
}