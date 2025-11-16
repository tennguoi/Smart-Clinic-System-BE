package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "examination_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExaminationRoom {

    @Id
    @GeneratedValue
    @Column(name = "room_id", columnDefinition = "CHAR(36)")
    private UUID roomId;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    // Bác sĩ phụ trách phòng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", columnDefinition = "CHAR(36)")
    private User doctor;

    // Bệnh nhân / hàng đợi đang khám (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_queue_id", columnDefinition = "CHAR(36)")
    private Queue currentQueue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status; // Available / Occupied

    @Column(name = "is_active")
    private Boolean isActive = true; // default true

    // Enum trạng thái phòng khám
    public enum RoomStatus {
        Available,
        Occupied
    }
}
