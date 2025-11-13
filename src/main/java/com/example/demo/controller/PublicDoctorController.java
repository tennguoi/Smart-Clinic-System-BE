package com.example.demo.controller;

import com.example.demo.dto.DoctorPublicResponse;
import com.example.demo.service.PublicDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/doctors")
public class PublicDoctorController {

    @Autowired
    private PublicDoctorService publicDoctorService;

    @GetMapping
    public ResponseEntity<List<DoctorPublicResponse>> getAllDoctors() {
        List<DoctorPublicResponse> doctors = publicDoctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }
}
