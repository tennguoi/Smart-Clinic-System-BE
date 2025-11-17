package com.example.demo.controller;

import com.example.demo.dto.AddPrescriptionRequest;
import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.dto.MedicalRecordDetailResponse;
import com.example.demo.dto.UpdateMedicalRecordRequest;
import com.example.demo.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/prescription")
    public ResponseEntity<?> addPrescription(@RequestBody AddPrescriptionRequest request) {
        try {
            medicalRecordService.addPrescriptionToRecord(request);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi thêm đơn thuốc: " + e.getMessage());
        }
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

    @GetMapping("/{recordId}")
    public ResponseEntity<MedicalRecordDetailResponse> getRecordDetail(@PathVariable UUID recordId) {
        try {
            MedicalRecordDetailResponse response = medicalRecordService.getRecordDetail(recordId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<MedicalRecordDetailResponse> updateRecord(
            @PathVariable UUID recordId,
            @RequestBody UpdateMedicalRecordRequest request) {
        try {
            MedicalRecordDetailResponse response = medicalRecordService.updateRecord(recordId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(@PathVariable UUID recordId) {
        try {
            medicalRecordService.deleteRecord(recordId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}