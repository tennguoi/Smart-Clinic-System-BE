package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsSeriesItem {
    private String label;
    private long visits;
    private Double avgTime;
    private Double completion;
    private String note;
}

