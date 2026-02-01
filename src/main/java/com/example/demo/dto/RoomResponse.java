package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private UUID roomId;
    private String roomName;
    private String status;          // Available / Occupied
    private Boolean isActive;
    private UUID currentQueueId;    // Bệnh nhân hiện tại (nếu có)

    private UUID doctorId;
    private String doctorName;
}
