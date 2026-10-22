package com.example.demo.controller.reception;

import com.example.demo.dto.GenericResponse;
import com.example.demo.dto.PatientResponse;
import com.example.demo.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reception/patients")
@PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
public class ReceptionistPatientController {

    private final PatientService patientService;

    @Autowired
    public ReceptionistPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<?> getAllPatients() {
        try {
            List<PatientResponse> patients = patientService.getAllPatients();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            String errorMessage = "Lỗi khi lấy danh sách bệnh nhân: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, errorMessage));
        }
    }
}