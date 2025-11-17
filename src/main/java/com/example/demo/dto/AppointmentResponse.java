package com.example.demo.dto;



import com.example.demo.entity.Appointment.Status;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID appointmentId,
        String patientName,
        Integer age,
        String phone,
        String email,
        LocalDateTime appointmentTime,
        Status status,
        String appointmentCode,
        String notes,
        Boolean quickBook,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        String confirmedByName
) {}
