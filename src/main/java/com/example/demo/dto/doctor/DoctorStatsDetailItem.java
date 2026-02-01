package com.example.demo.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDetailItem {
    private UUID queueId;
    private String patientName;
    private String patientPhone;
    private String status;
    private String roomName;
    private String checkInTime;
    private String startTime;
    private String endTime;
    private Long durationMinutes;
    private String note;
}

