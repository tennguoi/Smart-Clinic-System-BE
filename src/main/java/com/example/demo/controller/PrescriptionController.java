package com.example.demo.controller;

import com.example.demo.dto.PrescriptionRequest;
import com.example.demo.dto.PrescriptionResponse;
import com.example.demo.service.PrescriptionService;
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
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('BAC_SI')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            PrescriptionResponse response = prescriptionService.createPrescription(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable UUID prescriptionId) {
        try {
            PrescriptionResponse response = prescriptionService.getPrescriptionById(prescriptionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{prescriptionCode}")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByCode(@PathVariable String prescriptionCode) {
        try {
            PrescriptionResponse response = prescriptionService.getPrescriptionByCode(prescriptionCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            if (page != null && size != null) {
                Page<PrescriptionResponse> response = prescriptionService.getPrescriptionsByPatient(patientId, page, size);
                return ResponseEntity.ok(response.getContent());
            } else {
                List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByPatient(patientId);
                return ResponseEntity.ok(response);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasRole('BAC_SI')")
    public ResponseEntity<List<PrescriptionResponse>> getMyPrescriptions(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByDoctor(token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

