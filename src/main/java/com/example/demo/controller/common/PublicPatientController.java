package com.example.demo.controller.common;

import com.example.demo.dto.PatientResponse;
import com.example.demo.entity.Patient;
import com.example.demo.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/patients")
@RequiredArgsConstructor
public class PublicPatientController {

    private final PatientRepository patientRepository;

    /**
     * Lấy danh sách bệnh nhân (public - không cần authentication)
     * Hỗ trợ tìm kiếm theo tên hoặc số điện thoại
     */
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "") String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Patient> patients;

        if (search == null || search.trim().isEmpty()) {
            // Lấy tất cả
            patients = patientRepository.findAll(pageable);
        } else {
            // Tìm kiếm theo tên hoặc số điện thoại
            patients = patientRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(
                    search.trim(), search.trim(), pageable);
        }

        // Convert sang DTO sử dụng PatientResponse constructor
        Page<PatientResponse> response = patients.map(PatientResponse::new);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin chi tiết 1 bệnh nhân
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        return ResponseEntity.ok(new PatientResponse(patient));
    }
}