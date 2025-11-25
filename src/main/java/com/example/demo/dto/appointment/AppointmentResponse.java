// src/main/java/com/example/demo/dto/AppointmentResponse.java
package com.example.demo.dto.appointment;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Appointment.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        List<String> serviceIds,
        List<ServiceResponse> services,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        String confirmedByName
) {}
