package com.example.demo.controller;

import com.example.demo.dto.ClinicRequest;
import com.example.demo.dto.ClinicResponse;
import com.example.demo.service.ClinicService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clinic")
public class AdminClinicController {
    
    private final ClinicService clinicService;
    
    public AdminClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }
    
    /**
     * Lấy thông tin phòng khám
     */
    @GetMapping
    public ResponseEntity<ClinicResponse> getClinicInfo() {
        ClinicResponse response = clinicService.getClinicInfo();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cập nhật thông tin phòng khám
     */
    @PutMapping
    public ResponseEntity<ClinicResponse> updateClinicInfo(@Valid @RequestBody ClinicRequest request) {
        return ResponseEntity.ok(clinicService.updateClinicInfo(request));
    }
}

