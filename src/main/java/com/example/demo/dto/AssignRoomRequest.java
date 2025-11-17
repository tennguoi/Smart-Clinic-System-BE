package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

// === REQUEST ===

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoomRequest {
    private UUID queueId;    // ID của bệnh nhân trong hàng đợi
    private UUID roomId;     // ID của phòng khám
}