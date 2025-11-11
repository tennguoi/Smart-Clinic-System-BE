package com.example.demo.controller;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/doctors")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:5174"})
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<DoctorResponse> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.demo.dto.GenericResponse(false, "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage()));
        }
    }
}
