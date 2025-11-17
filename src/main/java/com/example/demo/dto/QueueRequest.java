package com.example.demo.dto;

import java.util.UUID;

public record QueueRequest(
        UUID patientId,
        UUID appointmentId,
        String priority // Emergency, Urgent, Normal
) {}

