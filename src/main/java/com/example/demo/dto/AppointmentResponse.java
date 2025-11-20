package com.example.demo.dto;



import java.time.LocalDateTime;
import java.util.UUID;

import com.example.demo.entity.Appointment.Status;

public record AppointmentResponse(
        UUID appointmentId,
        String patientName,
        String phone,
        String email,
        LocalDateTime appointmentTime,
        Status status,
        String appointmentCode,
        String notes,
        Boolean quickBook,
        java.util.List<String> serviceIds,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        String confirmedByName
) {}
