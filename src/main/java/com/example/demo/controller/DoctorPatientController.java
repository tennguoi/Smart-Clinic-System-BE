// com/example/demo/controller/DoctorPatientController.java
package com.example.demo.controller;

import com.example.demo.entity.Patient;
import com.example.demo.service.DoctorPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/patients")
public class DoctorPatientController {

    @Autowired
    private DoctorPatientService doctorPatientService;

    // GET: /api/doctor/patients/my?page=0&size=10
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPatients(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sort) {

        String doctorId = userDetails.getUsername(); // user_id của bác sĩ

        Page<Patient> patientPage = doctorPatientService.getMyPatients(doctorId, page, size, sort);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", patientPage.getContent());
        response.put("pagination", Map.of(
                "currentPage", patientPage.getNumber(),
                "totalPages", patientPage.getTotalPages(),
                "totalItems", patientPage.getTotalElements(),
                "pageSize", patientPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    // Tìm kiếm bệnh nhân của tôi
    @GetMapping("/my/search")
    public ResponseEntity<Map<String, Object>> searchMyPatients(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        String doctorId = userDetails.getUsername();
        List<Patient> patients = doctorPatientService.searchMyPatients(doctorId, keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", patients);
        response.put("total", patients.size());

        return ResponseEntity.ok(response);
    }
}