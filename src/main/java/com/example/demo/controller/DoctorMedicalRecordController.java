package com.example.demo.controller;

import com.example.demo.dto.MedicalRecordSearchCriteria;
import com.example.demo.dto.*;
import com.example.demo.dto.prescription.AddPrescriptionRequest;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.service.MedicalRecordService;
import com.example.demo.service.PdfExportService;
import com.example.demo.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/medical-records")
public class DoctorMedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PdfExportService pdfExportService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final JwtUtil jwtUtil;;

    public DoctorMedicalRecordController(MedicalRecordService medicalRecordService,
                                         PdfExportService pdfExportService,
                                         MedicalRecordRepository medicalRecordRepository,
                                         JwtUtil jwtUtil) {
        this.medicalRecordService = medicalRecordService;
        this.pdfExportService = pdfExportService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicalRecordResponse>> searchMine(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 1. Lấy token sạch (bỏ chữ Bearer)
        String token = extractToken(authorization);

        // 2. Lấy ID bác sĩ từ token
        UUID doctorId = jwtUtil.getUserIdFromToken(token);

        // 3. Đóng gói dữ liệu vào SearchCriteria
        MedicalRecordSearchCriteria criteria = new MedicalRecordSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);
        criteria.setDoctorId(doctorId); // Quan trọng: Gán ID bác sĩ để chỉ tìm hồ sơ của họ

        // 4. Gọi Service
        return ResponseEntity.ok(medicalRecordService.searchMyRecords(criteria));
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> listByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(medicalRecordService.listByPatient(patientId));
    }

    // GIỮ NGUYÊN TÊN CŨ
    @GetMapping("/mine")
    public ResponseEntity<List<MedicalRecordResponse>> listMine(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(medicalRecordService.listMyCreated(authorization));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<MedicalRecordDetailResponse> getRecordDetail(@PathVariable UUID recordId) {
        try {
            return ResponseEntity.ok(medicalRecordService.getRecordDetail(recordId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<MedicalRecordDetailResponse> updateRecord(
            @PathVariable UUID recordId,
            @RequestBody UpdateMedicalRecordRequest request) {
        try {
            return ResponseEntity.ok(medicalRecordService.updateRecord(recordId, request));
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

    // Export PDF giữ nguyên 100%
    @GetMapping("/{recordId}/export-pdf")
    public ResponseEntity<?> exportRecordAsPdf(@PathVariable UUID recordId) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại"));
            byte[] pdf = pdfExportService.generateMedicalRecordPdf(record);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"HoSoBenhAn_" + recordId + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{recordId}/export-prescription")
    public ResponseEntity<?> exportPrescriptionAsPdf(@PathVariable UUID recordId) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại"));
            byte[] pdf = pdfExportService.generatePrescriptionPdfForRecord(record);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"DonThuoc_" + recordId + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
