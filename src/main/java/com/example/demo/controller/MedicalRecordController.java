package com.example.demo.controller;

import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BAC_SI')")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping
    public ResponseEntity<Page<MedicalRecordResponse>> getAllMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "examinationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<MedicalRecordResponse> records = medicalRecordService.getAllMedicalRecords(page, size, sortBy, sortDir);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/my-records")
    public ResponseEntity<Page<MedicalRecordResponse>> getMyMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "examinationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        Page<MedicalRecordResponse> records = medicalRecordService.getMedicalRecordsByDoctor(token, page, size, sortBy, sortDir);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<MedicalRecordResponse>> getPatientMedicalRecords(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "examinationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<MedicalRecordResponse> records = medicalRecordService.getMedicalRecordsByPatient(patientId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientHistory(@PathVariable UUID patientId) {
        List<MedicalRecordResponse> history = medicalRecordService.getPatientHistory(patientId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecordById(@PathVariable UUID id) {
        return medicalRecordService.getMedicalRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            MedicalRecordResponse created = medicalRecordService.createMedicalRecord(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable UUID id,
            @Valid @RequestBody MedicalRecordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            MedicalRecordResponse updated = medicalRecordService.updateMedicalRecord(id, request, token);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            medicalRecordService.deleteMedicalRecord(id, token);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportMedicalRecordToPDF(@PathVariable UUID id) {
        try {
            byte[] pdfContent = medicalRecordService.exportMedicalRecordToPDF(id);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"medical_record_" + id + ".pdf\"")
                    .body(pdfContent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/patient/{patientId}/export/pdf")
    public ResponseEntity<byte[]> exportPatientHistoryToPDF(@PathVariable UUID patientId) {
        try {
            byte[] pdfContent = medicalRecordService.exportPatientHistoryToPDF(patientId);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"patient_history_" + patientId + ".pdf\"")
                    .body(pdfContent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
