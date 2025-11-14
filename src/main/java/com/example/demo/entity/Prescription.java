package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescription_patient", columnList = "patient_id"),
        @Index(name = "idx_prescription_doctor", columnList = "doctor_id"),
        @Index(name = "idx_prescription_medical_record", columnList = "medical_record_id"),
        @Index(name = "idx_prescription_date", columnList = "prescribed_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @Column(name = "prescription_id", nullable = false, updatable = false)
    private UUID prescriptionId;

    @PrePersist
    public void prePersist() {
        if (prescriptionId == null) prescriptionId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // User with ROLE_BAC_SI

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "prescription_code", unique = true, nullable = false, length = 20)
    private String prescriptionCode;

    @Column(name = "prescribed_date", nullable = false)
    private LocalDateTime prescribedDate;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Chẩn đoán

    @Column(name = "treatment_instructions", columnDefinition = "TEXT")
    private String treatmentInstructions; // Hướng dẫn điều trị

    @Column(name = "lifestyle_advice", columnDefinition = "TEXT")
    private String lifestyleAdvice; // Lời khuyên về lối sống

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate; // Ngày tái khám

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions; // Hướng dẫn tái khám

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes; // Ghi chú thêm

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addItem(PrescriptionItem item) {
        items.add(item);
        item.setPrescription(this);
    }

    public void removeItem(PrescriptionItem item) {
        items.remove(item);
        item.setPrescription(null);
    }

    public enum Status {
        ACTIVE,    // Đang sử dụng
        COMPLETED, // Đã hoàn thành
        CANCELLED  // Đã hủy
    }
}
