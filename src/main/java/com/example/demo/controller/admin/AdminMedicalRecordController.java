// src/main/java/com/example/demo/controller/AdminMedicalRecordController.java

package com.example.demo.controller.admin;

import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/medical-records")
@PreAuthorize("hasRole('ADMIN')")  // Chỉ Admin mới vào được
public class AdminMedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public AdminMedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<MedicalRecordResponse>> getAllRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        List<MedicalRecordResponse> records = medicalRecordService.getAllRecords();
        return ResponseEntity.ok(records);
    }
}