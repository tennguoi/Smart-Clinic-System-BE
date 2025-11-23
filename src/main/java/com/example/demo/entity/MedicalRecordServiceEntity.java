// src/main/java/com/example/demo/entity/MedicalRecordService.java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_record_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecordServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice; // Giá tại thời điểm kê → cực kỳ quan trọng

    private String note;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}