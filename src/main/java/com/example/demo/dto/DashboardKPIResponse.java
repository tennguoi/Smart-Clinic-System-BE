package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardKPIResponse {
    private long totalAppointmentsToday;
    private double appointmentsGrowth; // % growth vs yesterday/last week

    private long totalPatientsToday; // Or total active patients
    private double patientsGrowth;

    private BigDecimal totalRevenueMonth; // Or today
    private double revenueGrowth;

    private double cancelRate; // %
    private double cancelRateGrowth;
}
