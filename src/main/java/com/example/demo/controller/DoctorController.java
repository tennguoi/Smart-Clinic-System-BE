package com.example.demo.controller;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * Lấy danh sách tất cả bác sĩ đang hoạt động
     * Public endpoint - không cần authentication
     * @return Danh sách bác sĩ
     */
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

    /**
     * Lấy thông tin chi tiết của một bác sĩ theo ID
     * Public endpoint - không cần authentication
     * @param doctorId ID của bác sĩ
     * @return Thông tin bác sĩ
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable("doctorId") UUID doctorId) {
        try {
            if (doctorId == null) {
                return ResponseEntity.badRequest()
                        .body(new com.example.demo.dto.GenericResponse(false, "ID bác sĩ không hợp lệ"));
            }
            
            DoctorResponse doctor = doctorService.getDoctorById(doctorId);
            return ResponseEntity.ok(doctor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new com.example.demo.dto.GenericResponse(false, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new com.example.demo.dto.GenericResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.demo.dto.GenericResponse(false, "Lỗi server khi lấy thông tin bác sĩ"));
        }
    }
}

