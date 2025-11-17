package com.example.demo.entity;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// truy vấn cho nhanh hơn vì vướng uuid
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
    @Column(name = "appointment_id", nullable = false, updatable = false)
    private UUID appointmentId;

    @PrePersist
    public void prePersist() {
        if (appointmentId == null) appointmentId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.Pending;
        if (quickBook == null) quickBook = true;
    }

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "appointment_code", nullable = false, unique = true, length = 64)
    private String appointmentCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "quick_book")
    private Boolean quickBook;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;




    public enum Status {
        Pending,      // Đang chờ duyệt
        Confirmed,    // Đã xác nhận lịch hẹn
        In_Progress,  // Đang khám
        Completed,    // Hoàn thành
        Cancelled     // Đã hủy
    }
}

