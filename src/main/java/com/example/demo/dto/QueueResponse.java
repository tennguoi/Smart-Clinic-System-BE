package com.example.demo.dto;

import com.example.demo.entity.Queue;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueResponse(
        UUID queueId,
        UUID patientId,
        String patientName,
        String patientCode,
        UUID assignedRoomId,
        String roomNumber,
        UUID appointmentId,
        String appointmentCode,
        String queueNumber,
        Queue.Status status,
        Queue.Priority priority,
        LocalDateTime checkInTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt
) {}

