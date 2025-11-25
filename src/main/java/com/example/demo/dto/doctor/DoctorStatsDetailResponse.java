package com.example.demo.dto.doctor;

import com.example.demo.dto.doctor.DoctorStatsDetailItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDetailResponse {
    private List<DoctorStatsDetailItem> items;
    private long totalItems;
}

