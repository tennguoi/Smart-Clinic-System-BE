package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrentPatientDTO {
    private String queueNumber;
    private String fullName;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String priority;
    private LocalDateTime checkInTime;
    private LocalDateTime startTime;
}
