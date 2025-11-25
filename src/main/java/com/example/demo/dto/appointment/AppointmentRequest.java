// src/main/java/com/example/demo/dto/AppointmentRequest.java
package com.example.demo.dto.appointment;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentRequest(
        String patientName,
        String phone,
        String email,
        LocalDateTime appointmentTime,
        String notes,
        Boolean quickBook,
        List<String> serviceIds
) {}
