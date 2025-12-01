package com.example.demo.controller.admin;

import com.example.demo.dto.TopServiceDTO;
import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/top-services")
    public ResponseEntity<List<TopServiceDTO>> getTopServices() {
        return ResponseEntity.ok(dashboardService.getTop5Services());
    }

    @GetMapping("/stats")
    public ResponseEntity<com.example.demo.dto.DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/kpi")
    public ResponseEntity<com.example.demo.dto.DashboardKPIResponse> getDashboardKPIs() {
        return ResponseEntity.ok(dashboardService.getDashboardKPIs());
    }

    @GetMapping("/trend/appointments")
    public ResponseEntity<List<com.example.demo.dto.TrendDataPoint>> getAppointmentTrend() {
        // Default to 7 days
        return ResponseEntity.ok(dashboardService.getAppointmentTrend(7));
    }

    @GetMapping("/trend/revenue")
    public ResponseEntity<List<com.example.demo.dto.TrendDataPoint>> getRevenueTrend() {
        // Default to 7 days
        return ResponseEntity.ok(dashboardService.getRevenueTrend(7));
    }
}