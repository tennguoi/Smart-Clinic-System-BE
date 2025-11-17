package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Queue {

    @Id
    @GeneratedValue
    @Column(name = "queue_id", columnDefinition = "CHAR(36)")
    private UUID queueId;

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", columnDefinition = "CHAR(36)")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", columnDefinition = "CHAR(36)")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_room_id", columnDefinition = "CHAR(36)")
    private ExaminationRoom assignedRoom;

    // --- Fields ---
    @Column(name = "queue_number", length = 20)
    private String queueNumber;  // VD: A001, B015

    @Enumerated(EnumType.STRING)
    private Priority priority;   // Normal / Urgent / Emergency

    private LocalDateTime checkInTime;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;  // Waiting / InProgress / Completed / Cancelled

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    // Auto set createdAt
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // === ENUMS ===
    public enum Priority {
        Normal("Bình thường"),
        Urgent("Ưu tiên"),
        Emergency("Khẩn cấp");

        private final String label;

        Priority(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum QueueStatus {
        Waiting("Đang chờ"),
        InProgress("Đang khám"),
        Completed("Hoàn tất"),
        Cancelled("Hủy");

        private final String label;

        QueueStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

}
