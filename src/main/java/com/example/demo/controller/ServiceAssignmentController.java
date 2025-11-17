package com.example.demo.controller;

import com.example.demo.dto.ServiceAssignmentRequest;
import com.example.demo.entity.ServiceAssignment;
import com.example.demo.service.ServiceAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BAC_SI')")
public class ServiceAssignmentController {

    private final ServiceAssignmentService serviceAssignmentService;

    @PostMapping
    public ResponseEntity<ServiceAssignment> createServiceAssignment(
            @Valid @RequestBody ServiceAssignmentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            ServiceAssignment assignment = serviceAssignmentService.createServiceAssignment(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<List<ServiceAssignment>> getServiceAssignmentsByMedicalRecord(
            @PathVariable UUID medicalRecordId) {
        try {
            List<ServiceAssignment> assignments = 
                    serviceAssignmentService.getServiceAssignmentsByMedicalRecord(medicalRecordId);
            return ResponseEntity.ok(assignments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ServiceAssignment>> getServiceAssignmentsByPatient(
            @PathVariable UUID patientId) {
        try {
            List<ServiceAssignment> assignments = 
                    serviceAssignmentService.getServiceAssignmentsByPatient(patientId);
            return ResponseEntity.ok(assignments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

