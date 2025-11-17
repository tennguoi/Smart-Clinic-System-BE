package com.example.demo.controller;

import com.example.demo.dto.GenericResponse;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reception/queue")
@PreAuthorize("hasRole('ROLE_TIEP_TAN')")
public class ReceptionistQueueController {

    private final QueueService queueService;

    @Autowired
    public ReceptionistQueueController(QueueService queueService) {
        this.queueService = queueService;
    }
    @GetMapping("/{queueId}")
    public ResponseEntity<?> getQueueById(@PathVariable UUID queueId) {
        Optional<QueuePatientResponse> result = queueService.getQueueById(queueId);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // --- Danh sách chờ ---
    @GetMapping("/waiting")
    public ResponseEntity<?> getWaitingPatients() {
        List<QueuePatientResponse> list = queueService.getWaitingPatients();
        return ResponseEntity.ok(list);
    }

    // --- Thêm mới ---
    @PostMapping("/add")
    public ResponseEntity<?> addQueue(@RequestBody QueuePatientResponse dto) {
        try {
            QueuePatientResponse result = queueService.addQueuePatient(dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi validation (enum, date format, etc.)
            return ResponseEntity.badRequest()
                    .body(new GenericResponse(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý các lỗi khác (database, constraint, etc.)
            String errorMessage = "Lỗi khi thêm bệnh nhân vào hàng đợi: " + e.getMessage();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, errorMessage));
        }
    }

    // --- Xoá ---
    @DeleteMapping("/{queueId}")
    public ResponseEntity<?> deleteQueue(@PathVariable UUID queueId) {
        boolean success = queueService.deleteQueuePatient(queueId);
        return ResponseEntity.ok(new GenericResponse(success, success ? "Xóa thành công" : "Không tìm thấy hàng đợi"));
    }

    // --- Sửa trạng thái nhanh ---
    @PatchMapping("/{queueId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID queueId, @RequestParam String status) {
        return ResponseEntity.ok(queueService.updateQueueStatus(queueId, status));
    }

    // --- Sửa toàn bộ thông tin ---
    @PutMapping("/{queueId}")
    public ResponseEntity<?> updateQueue(@PathVariable UUID queueId, @RequestBody QueuePatientResponse dto) {
        return ResponseEntity.ok(queueService.updateQueuePatient(queueId, dto));
    }

    // --- Tìm kiếm ---
    @GetMapping("/search")
    public ResponseEntity<?> searchQueue(@RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String status) {
        return ResponseEntity.ok(queueService.searchQueue(phone, status));
    }
}
