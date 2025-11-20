package com.example.demo.dto;
import java.time.LocalDateTime;



public record AppointmentRequest(
        String patientName,
        String phone,
        String email,
        LocalDateTime appointmentTime,
        String notes,
        java.util.List<String> serviceIds
) {}
