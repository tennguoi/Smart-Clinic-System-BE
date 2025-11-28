package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {
    private UUID clinicId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private LocalTime morningStartTime;
    private LocalTime morningEndTime;
    private LocalTime afternoonStartTime;
    private LocalTime afternoonEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

