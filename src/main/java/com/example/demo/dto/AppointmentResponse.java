// src/main/java/com/example/demo/dto/AppointmentResponse.java
package com.example.demo.dto;

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
        List<String> serviceIds,                    // vẫn trả về list ID nếu cần
        List<ServiceResponse> services,             // chi tiết dịch vụ
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        String confirmedByName
) {}