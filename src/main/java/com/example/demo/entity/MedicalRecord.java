package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_records", indexes = {
        @Index(name = "idx_medical_record_patient", columnList = "patient_id"),
        @Index(name = "idx_medical_record_doctor", columnList = "doctor_id"),
        @Index(name = "idx_medical_record_date", columnList = "examination_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @Column(name = "record_id", nullable = false, updatable = false)
    private UUID recordId;

    @PrePersist
    public void prePersist() {
        if (recordId == null) recordId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // User with ROLE_BAC_SI

    @Column(name = "examination_date", nullable = false)
    private LocalDateTime examinationDate;

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint; // Lý do khám

    @Column(name = "history_of_present_illness", columnDefinition = "TEXT")
    private String historyOfPresentIllness; // Quá trình bệnh lý

    @Column(name = "physical_examination", columnDefinition = "TEXT")
    private String physicalExamination; // Khám thể chất

    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns; // Dấu hiệu sinh tồn (huyết áp, mạch, nhiệt độ, cân nặng...)

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Chẩn đoán

    @Column(name = "differential_diagnosis", columnDefinition = "TEXT")
    private String differentialDiagnosis; // Chẩn đoán phân biệt

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan; // Phác đồ điều trị

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications; // Thuốc kê đơn

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions; // Hướng dẫn tái khám

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes; // Ghi chú thêm

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        DRAFT,      // Bản nháp
        COMPLETED,  // Hoàn thành
        ARCHIVED    // Lưu trữ
    }
}
