package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AddPrescriptionRequest;
import com.example.demo.dto.MedicalRecordDetailResponse;
import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.dto.UpdateMedicalRecordRequest;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.service.MedicalRecordService;
import com.example.demo.service.PdfExportService;

@RestController
@RequestMapping("/api/doctor/medical-records")
public class DoctorMedicalRecordController {
    private final MedicalRecordService medicalRecordService;
    private final PdfExportService pdfExportService;
    private final MedicalRecordRepository medicalRecordRepository;

    public DoctorMedicalRecordController(MedicalRecordService medicalRecordService,
                                        PdfExportService pdfExportService,
                                        MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordService = medicalRecordService;
        this.pdfExportService = pdfExportService;
        this.medicalRecordRepository = medicalRecordRepository;
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

    @GetMapping("/{recordId}/export-pdf")
    public ResponseEntity<?> exportRecordAsPdf(@PathVariable UUID recordId) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại"));
            
            byte[] pdfContent = pdfExportService.generateMedicalRecordPdf(record);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"HoSoBenhAn_" + recordId + ".pdf\"")
                    .body(pdfContent);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xuất PDF: " + e.getMessage());
        }
    }

    @GetMapping("/{recordId}/export-prescription")
    public ResponseEntity<?> exportPrescriptionAsPdf(@PathVariable UUID recordId) {
        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại"));

            byte[] pdfContent = pdfExportService.generatePrescriptionPdfForRecord(record);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"DonThuoc_" + recordId + ".pdf\"")
                    .body(pdfContent);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xuất PDF: " + e.getMessage());
        }
    }
}