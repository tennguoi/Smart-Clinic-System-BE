package com.example.demo.dto.appointment;


import java.time.LocalDateTime;
import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AppointmentCheckInDTO {
    private UUID appointmentId;
    private String appointmentCode;
    private String patientName;
    private String phone;
    private String email;
    private LocalDateTime appointmentTime;
    private String notes;
    private boolean hasCheckedIn;
}