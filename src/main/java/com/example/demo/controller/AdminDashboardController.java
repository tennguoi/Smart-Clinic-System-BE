package com.example.demo.controller;

import com.example.demo.dto.TopServiceDTO;
import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/top-services")
    public ResponseEntity<List<TopServiceDTO>> getTopServices(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getTop5Services(startDate, endDate));
    }

    @GetMapping("/stats")
    public ResponseEntity<com.example.demo.dto.DashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(startDate, endDate));
    }

    @GetMapping("/kpi")
    public ResponseEntity<com.example.demo.dto.DashboardKPIResponse> getDashboardKPIs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getDashboardKPIs(startDate, endDate));
    }

    @GetMapping("/trend/appointments")
    public ResponseEntity<List<com.example.demo.dto.TrendDataPoint>> getAppointmentTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getAppointmentTrend(startDate, endDate));
    }

    @GetMapping("/trend/revenue")
    public ResponseEntity<List<com.example.demo.dto.TrendDataPoint>> getRevenueTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getRevenueTrend(startDate, endDate));
    }
}