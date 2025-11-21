package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsKpis {
    private long totalVisits;
    private Double averageDurationMinutes;
    private Double completionRate;
}

