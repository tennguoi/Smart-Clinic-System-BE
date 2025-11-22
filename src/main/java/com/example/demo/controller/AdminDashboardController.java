package com.example.demo.controller;

import com.example.demo.dto.TopServiceDTO;
import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") // Chỉ Admin mới xem được
    public ResponseEntity<List<TopServiceDTO>> getTopServices() {
        return ResponseEntity.ok(dashboardService.getTop5Services());
    }
}