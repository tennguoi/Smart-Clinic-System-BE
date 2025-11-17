package com.example.demo.controller;

import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/medical-records")
public class DoctorMedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public DoctorMedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.ok(medicalRecordService.create(authorization, request));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> listByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(medicalRecordService.listByPatient(patientId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MedicalRecordResponse>> listMine(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(medicalRecordService.listMyCreated(authorization));
    }
}