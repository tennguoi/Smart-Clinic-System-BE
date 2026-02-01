package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_code", columnList = "appointment_code", unique = true),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "appointment_id", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID appointmentId;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "id_number", length = 20)
    private String idNumber;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "appointment_code", unique = true, length = 64)
    private String appointmentCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "service_ids", columnDefinition = "TEXT")
    private String serviceIds;

    @Column(name = "quick_book")
    private Boolean quickBook;

    @Column(name = "reminder_sent")
    private Boolean reminderSent;

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

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.Pending;
        if (quickBook == null) quickBook = true;
        if (reminderSent == null) reminderSent = false;
    }
}