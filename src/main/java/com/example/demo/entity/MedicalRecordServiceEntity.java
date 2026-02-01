// src/main/java/com/example/demo/entity/MedicalRecordServiceEntity.java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_record_services", indexes = {
        @Index(name = "idx_mrs_patient", columnList = "patient_id"),      // SIÊU NHANH khi tìm theo bệnh nhân
        @Index(name = "idx_mrs_record", columnList = "record_id"),
        @Index(name = "idx_mrs_service", columnList = "service_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecordServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // TỐI ƯU: Chỉ lưu patient_id, không lưu cả object Patient
    @Column(name = "patient_id", nullable = false, updatable = false)
    private UUID patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // TỰ ĐỘNG GÁN patientId khi save → không bao giờ sai!
    @PrePersist
    private void ensurePatientId() {
        if (this.patientId == null && this.medicalRecord != null && this.medicalRecord.getPatient() != null) {
            this.patientId = this.medicalRecord.getPatient().getPatientId();
        }
    }
}