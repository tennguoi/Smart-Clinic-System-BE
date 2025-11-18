package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_code", columnList = "appointment_code", unique = true),
        @Index(name = "idx_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @Column(name = "appointment_id", nullable = false, updatable = false)
    private UUID appointmentId;

    @PrePersist
    public void prePersist() {
        if (appointmentId == null) appointmentId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.Pending;
        if (quickBook == null) quickBook = true;
    }

    // NOT NULL
    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    // CÓ THỂ NULL
    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "appointment_code", unique = true, length = 64)
    private String appointmentCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "quick_book")
    private Boolean quickBook;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    public enum Status {
        Pending, Confirmed, Cancelled
    }
}