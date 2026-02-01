package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {
    @Id
    @Column(name = "record_id", nullable = false, updatable = false)
    private UUID recordId;

    // ✅ QUAN HỆ VỚI PATIENT (bảng patients)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "patient_id")
    private Patient patient;

    @Column(name = "patient_name", length = 255)
    private String patientName;

    // ✅ QUAN HỆ VỚI DOCTOR (bảng users)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", referencedColumnName = "user_id", nullable = false)
    private User doctor;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ LIÊN KẾT VỚI LƯỢT KHÁM (QUEUE)
    @Column(name = "queue_id", columnDefinition = "CHAR(36)")
    private UUID queueId;

    @PrePersist
    public void prePersist() {
        if (recordId == null)
            recordId = UUID.randomUUID();
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}