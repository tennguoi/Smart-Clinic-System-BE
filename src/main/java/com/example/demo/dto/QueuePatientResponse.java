package com.example.demo.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueuePatientResponse {

    // Queue info
    private UUID queueId;
    private String queueNumber;
    private String priority;
    private String status;
    private String checkInTime;

    // Patient info
    private UUID patientId;
    private String patientName;
    private String phone;
    private String email;
    private LocalDate dob;
    private String gender;
    private String address;
}
