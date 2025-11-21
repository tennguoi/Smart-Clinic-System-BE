package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsResponse {
    private DoctorStatsKpis kpis;
    private List<DoctorStatsSeriesItem> series;
    private List<DoctorStatsSeriesItem> table;
    private Long totalItems;
}

