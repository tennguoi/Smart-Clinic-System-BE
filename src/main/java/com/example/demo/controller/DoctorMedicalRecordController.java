package com.example.demo.controller;

import com.example.demo.dto.AddPrescriptionRequest; // <<< THÊM DÒNG NÀY
import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.service.MedicalRecordService;
import org.springframework.http.HttpStatus; // <<< THÊM DÒNG NÀY
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/medical-records") // URL gốc
public class DoctorMedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public DoctorMedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * API CŨ (của đồng đội): Tạo hồ sơ bệnh án
     * POST /api/doctor/medical-records
     */
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.ok(medicalRecordService.create(authorization, request));
    }

    //
    // --- PHẦN CODE MỚI CỦA BẠN (Thêm vào) ---
    //
    /**
     * API MỚI (của bạn): Thêm toa thuốc (Prescription) vào hồ sơ đã có
     * POST /api/doctor/medical-records/prescription
     */
    @PostMapping("/prescription") // <<< THÊM API MỚI
    public ResponseEntity<?> addPrescription(
            @RequestBody AddPrescriptionRequest request) { // <<< Dùng DTO mới của bạn

        // Gọi hàm mới 'addPrescriptionToRecord' trong Service
        medicalRecordService.addPrescriptionToRecord(request);

        // Trả về mã 201 Created (vì đã tạo mới đơn thuốc)
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    //
    // --- HẾT PHẦN CODE MỚI ---
    //

    /**
     * API CŨ: Lấy hồ sơ theo ID bệnh nhân
     * GET /api/doctor/medical-records/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> listByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(medicalRecordService.listByPatient(patientId));
    }

    /**
     * API CŨ: Lấy hồ sơ của bác sĩ (đang đăng nhập)
     * GET /api/doctor/medical-records/mine
     */
    @GetMapping("/mine")
    public ResponseEntity<List<MedicalRecordResponse>> listMine(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(medicalRecordService.listMyCreated(authorization));
    }
}