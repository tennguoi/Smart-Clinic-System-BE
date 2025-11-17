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
@Table(name = "queues", indexes = {
        @Index(name = "idx_queue_patient", columnList = "patient_id"),
        @Index(name = "idx_queue_room", columnList = "assigned_room_id"),
        @Index(name = "idx_queue_appointment", columnList = "appointment_id"),
        @Index(name = "idx_queue_status", columnList = "status"),
        @Index(name = "idx_queue_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Queue {

    @Id
    @Column(name = "queue_id", nullable = false, updatable = false)
    private UUID queueId;

    @PrePersist
    public void prePersist() {
        if (queueId == null) queueId = UUID.randomUUID();
        if (checkInTime == null) checkInTime = LocalDateTime.now();
        if (status == null) status = Status.Waiting;
        if (priority == null) priority = Priority.Normal;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_room_id")
    private ExaminationRoom assignedRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "queue_number", nullable = false, unique = true, length = 20)
    private String queueNumber; // VD: A001

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "start_time")
    private LocalDateTime startTime; // Thời gian bắt đầu khám

    @Column(name = "end_time")
    private LocalDateTime endTime; // Thời gian hoàn thành khám

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    public enum Status {
        Waiting,      // Đang chờ
        InProgress,   // Đang khám
        Completed,    // Hoàn thành
        Cancelled     // Đã hủy
    }

    public enum Priority {
        Emergency,    // Cấp cứu
        Urgent,       // Khẩn cấp
        Normal        // Bình thường
    }
}

