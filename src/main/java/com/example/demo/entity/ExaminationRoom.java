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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "examination_rooms", indexes = {
        @Index(name = "idx_room_number", columnList = "room_number", unique = true),
        @Index(name = "idx_room_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExaminationRoom {

    @Id
    @Column(name = "room_id", nullable = false, updatable = false)
    private UUID roomId;

    @PrePersist
    public void prePersist() {
        if (roomId == null) roomId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = Status.Available;
    }

    @Column(name = "room_number", nullable = false, unique = true, length = 20)
    private String roomNumber; // VD: 101, 102

    @Column(name = "room_name", length = 100)
    private String roomName; // Tên phòng (tùy chọn)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_queue_id")
    private Queue currentQueue; // Bệnh nhân đang khám trong phòng này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id")
    private User assignedDoctor; // Bác sĩ được phân công (tùy chọn)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        Available,  // Trống
        Occupied,   // Đang sử dụng
        Maintenance // Bảo trì
    }
}

