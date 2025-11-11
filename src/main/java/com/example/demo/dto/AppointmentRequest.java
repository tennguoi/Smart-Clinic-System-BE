package com.example.demo.dto;
import java.time.*;



public record AppointmentRequest(
        String patientName,
        String phone,
        String email,
        LocalDateTime appointmentTime,
        String notes
) {}
