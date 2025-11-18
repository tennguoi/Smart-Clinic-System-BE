package com.example.demo.controller;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.dto.AppointmentResponse;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Giữ lại nếu bạn có dùng
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

    // [MỚI] API Tạo lịch hẹn mới (cho form Lễ tân)
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        // Gọi service.create() để tạo lịch hẹn với quickBook = false
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    // [CẬP NHẬT] API Lấy danh sách & Tìm kiếm
    // Hỗ trợ lọc theo: Trạng thái, Số điện thoại, Tên bệnh nhân
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list(
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "patientName", required = false) String patientName // Thêm tham số này
    ) {
        Status s = status != null ? status : Status.Pending;
        // Gọi phương thức listBy mới trong Service
        return ResponseEntity.ok(service.listBy(s, phone, patientName));
    }

    // API Cập nhật trạng thái (Xác nhận/Hủy)
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}