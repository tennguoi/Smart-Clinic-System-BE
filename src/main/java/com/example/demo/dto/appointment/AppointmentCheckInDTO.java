package com.example.demo.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCheckInDTO {
    private java.util.UUID appointmentId;
    private String appointmentCode;
    private String patientName;
    private String phone;
    private String email;
    private LocalDateTime appointmentTime;
    private String notes;
    private boolean hasCheckedIn;

    private List<ServiceInfo> services;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private String serviceId;  // ĐÃ ĐÚNG – String, không lỗi
        private String name;
    }
}