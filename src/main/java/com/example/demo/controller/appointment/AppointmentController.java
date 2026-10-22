// src/main/java/com/example/demo/controller/AppointmentController.java
package com.example.demo.controller.appointment;

import com.example.demo.dto.appointment.AppointmentRequest;
import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    /**
     * Lấy danh sách lịch hẹn cho lễ tân (có lọc & phân trang)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<Page<AppointmentResponse>> searchAppointments(
            @RequestParam(defaultValue = "Pending") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(service.searchAppointments(status, keyword, page, size));
    }

    /**
     * Lễ tân tạo lịch hẹn
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    /**
     * Lễ tân sửa thông tin lịch hẹn
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequest req
    ) {
        return ResponseEntity.ok(service.updateInfo(id, req));
    }

    /**
     * Lễ tân cập nhật trạng thái lịch hẹn
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam Status status
    ) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}
