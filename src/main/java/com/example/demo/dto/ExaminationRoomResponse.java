package com.example.demo.dto;

import com.example.demo.entity.ExaminationRoom;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExaminationRoomResponse(
        UUID roomId,
        String roomNumber,
        String roomName,
        ExaminationRoom.Status status,
        UUID currentQueueId,
        String currentQueueNumber,
        UUID assignedDoctorId,
        String assignedDoctorName,
        LocalDateTime createdAt
) {}

