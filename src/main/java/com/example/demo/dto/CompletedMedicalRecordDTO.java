package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompletedMedicalRecordDTO {
    private UUID recordId;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private String diagnosis;
    private String treatmentNotes;
    private LocalDateTime createdAt;
    private List<ServiceItemDTO> services;
    private BigDecimal totalAmount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ServiceItemDTO {
        private String serviceId;
        private String serviceName;
        private String serviceCategory;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String note;
    }
}
