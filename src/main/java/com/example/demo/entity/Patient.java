package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_phone", columnList = "phone"),
        @Index(name = "idx_patient_email", columnList = "email"),
        @Index(name = "idx_patient_code", columnList = "patient_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @Column(name = "patient_id", nullable = false, updatable = false)
    private UUID patientId;

    @PrePersist
    public void prePersist() {
        if (patientId == null) patientId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    @Column(name = "patient_code", nullable = false, unique = true, length = 20)
    private String patientCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "identity_card", length = 20)
    private String identityCard;

    @Column(name = "insurance_number", length = 20)
    private String insuranceNumber;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
