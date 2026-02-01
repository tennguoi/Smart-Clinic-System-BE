package com.example.demo.controller;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Lấy hồ sơ khám mới nhất của bệnh nhân
     * Endpoint: GET /api/medical-records/patient/{patientId}/latest
     */
    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<Map<String, Object>> getLatestRecordByPatient(@PathVariable UUID patientId) {
        MedicalRecord record = medicalRecordRepository
                .findFirstByPatient_PatientIdOrderByCreatedAtDesc(patientId)
                .orElseThrow(() -> new RuntimeException("Bệnh nhân chưa có hồ sơ khám nào"));

        Map<String, Object> response = new HashMap<>();
        response.put("recordId", record.getRecordId());
        response.put("patientId", patientId);
        response.put("patientName", record.getPatient().getFullName());
        response.put("createdAt", record.getCreatedAt());
        response.put("diagnosis", record.getDiagnosis());

        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra xem bệnh nhân đã có hồ sơ khám chưa
     */
    @GetMapping("/patient/{patientId}/exists")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAC_SI', 'TIEP_TAN', 'Y_TA')")
    public ResponseEntity<Map<String, Boolean>> checkPatientHasRecord(@PathVariable UUID patientId) {
        boolean hasRecord = medicalRecordRepository.existsByPatient_PatientId(patientId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasRecord", hasRecord);

        return ResponseEntity.ok(response);
    }
}