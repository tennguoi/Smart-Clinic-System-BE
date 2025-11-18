package com.example.demo.controller;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.dto.AppointmentResponse;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.service.AppointmentService;
import org.springframework.http.HttpStatus; // Thêm import này
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // --- PHƯƠNG THỨC MỚI ĐƯỢC THÊM VÀO ---
    // Xử lý việc tạo lịch hẹn mới từ form (POST /api/appointments)
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        // Gọi service.create() mà chúng ta đã thêm vào
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    // --- HẾT PHẦN THÊM MỚI ---


    // --- PHƯƠNG THỨC NÀY ĐƯỢC CẬP NHẬT ---
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list(
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "phone", required = false) String phone // Thêm tham số phone
    ) {
        Status s = status != null ? status : Status.Pending;
        // Thay đổi lời gọi từ listByStatus sang listBy
        return ResponseEntity.ok(service.listBy(s, phone));
    }
    // --- HẾT PHẦN CẬP NHẬT ---

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}