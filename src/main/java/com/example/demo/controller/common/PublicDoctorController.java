package com.example.demo.controller.common;

import com.example.demo.dto.doctor.DoctorResponse;
import com.example.demo.dto.GenericResponse;
import com.example.demo.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/doctors")
public class PublicDoctorController {

    private final DoctorService doctorService;

    @Autowired
    public PublicDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<DoctorResponse> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            String errorMessage = "Lỗi khi lấy danh sách bác sĩ: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, errorMessage));
        }
    }
}

