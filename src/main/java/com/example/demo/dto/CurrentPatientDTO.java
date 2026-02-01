// src/main/java/com/example/demo/dto/CurrentPatientDTO.java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPatientDTO {
    private UUID queueId;
    private UUID patientId;
    private String queueNumber;
    private String fullName;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String priority;
    private LocalDateTime checkInTime;
    private LocalDateTime startTime;

    // THÊM DÒNG NÀY - QUAN TRỌNG NHẤT
    private String notes;  // ← Đây chính là "Triệu chứng / Lý do khám"
}