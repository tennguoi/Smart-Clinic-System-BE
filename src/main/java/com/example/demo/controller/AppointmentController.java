package com.example.demo.controller;

import com.example.demo.dto.appointment.AppointmentRequest;
import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list(
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "patientName", required = false) String patientName
    ) {
        Status s = status != null ? status : Status.Pending;
        return ResponseEntity.ok(service.listBy(s, phone, patientName));
    }

    // ======================================================
    // [MỚI] API Sửa thông tin lịch hẹn (PUT)
    // ======================================================
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateInfo(
            @PathVariable UUID id,
            @RequestBody AppointmentRequest request
    ) {
        return ResponseEntity.ok(service.updateInfo(id, request));
    }

    // API Cập nhật trạng thái (Xác nhận/Hủy)
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    // ======================================================
    // [MỚI - QUAN TRỌNG] XỬ LÝ LỖI NGAY TẠI ĐÂY
    // Hàm này sẽ tự động chạy khi Service báo lỗi "IllegalArgumentException"
    // ======================================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage()); // Lấy câu tiếng Việt: "Thời gian hẹn phải từ..."

        // Trả về lỗi 400 Bad Request kèm thông báo JSON
        return ResponseEntity.badRequest().body(response);
    }
}