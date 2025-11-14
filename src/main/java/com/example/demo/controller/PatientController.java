package com.example.demo.controller;

import com.example.demo.dto.PatientRequest;
import com.example.demo.dto.PatientResponse;
import com.example.demo.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN', 'BAC_SI')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        
        Page<PatientResponse> patients;
        if (search != null && !search.trim().isEmpty()) {
            patients = patientService.searchPatients(search.trim(), page, size, sortBy, sortDir);
        } else {
            patients = patientService.getAllActivePatients(page, size, sortBy, sortDir);
        }
        
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PatientResponse> getPatientByCode(@PathVariable String code) {
        return patientService.getPatientByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<PatientResponse> getPatientByPhone(@PathVariable String phone) {
        return patientService.getPatientByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            PatientResponse created = patientService.createPatient(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            PatientResponse updated = patientService.updatePatient(id, request, token);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
