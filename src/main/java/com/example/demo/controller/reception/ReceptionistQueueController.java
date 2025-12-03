package com.example.demo.controller.reception;

import com.example.demo.dto.GenericResponse;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.appointment.AppointmentCheckInDTO;
import com.example.demo.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    // =================================================================
    // ✨ API MỚI: THÊM BỆNH NHÂN TỪ LỊCH HẸN
    // =================================================================

    /**
     * Lấy danh sách lịch hẹn đã xác nhận trong ngày để thêm vào hàng chờ
     * GET /api/reception/queue/appointments/today
     */
    @GetMapping("/appointments/today")
    public ResponseEntity<?> getTodayAppointments() {
        try {
            List<AppointmentCheckInDTO> appointments =
                    queueService.getTodayConfirmedAppointments();
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, "Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage()));
        }
    }

    /**
     * Tạo hàng chờ từ lịch hẹn - Nhận thông tin bổ sung từ form
     * POST /api/reception/queue/check-in-appointment/{appointmentId}
     */
    @PostMapping("/check-in-appointment/{appointmentId}")
    public ResponseEntity<?> checkInFromAppointment(
            @PathVariable UUID appointmentId,
            @RequestBody(required = false) Map<String, Object> additionalInfo) {
        try {
            QueuePatientResponse result = queueService.createQueueFromAppointment(appointmentId, additionalInfo);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new GenericResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, "Lỗi khi tạo hàng chờ: " + e.getMessage()));
        }
    }

    // =================================================================
    // API CŨ - GIỮ NGUYÊN
    // =================================================================

    @GetMapping("/{queueId}")
    public ResponseEntity<?> getQueueById(@PathVariable UUID queueId) {
        Optional<QueuePatientResponse> result = queueService.getQueueById(queueId);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/waiting")
    public ResponseEntity<?> getWaitingPatients() {
        List<QueuePatientResponse> list = queueService.getWaitingPatients();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/waiting/unassigned")
    public ResponseEntity<?> getUnassignedWaitingPatients() {
        List<QueuePatientResponse> list = queueService.getUnassignedWaitingPatients();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addQueue(@RequestBody QueuePatientResponse dto) {
        try {
            QueuePatientResponse result = queueService.addQueuePatient(dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new GenericResponse(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            String errorMessage = "Lỗi khi thêm bệnh nhân vào hàng đợi: " + e.getMessage();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, errorMessage));
        }
    }

    @DeleteMapping("/{queueId}")
    public ResponseEntity<?> deleteQueue(@PathVariable UUID queueId) {
        boolean success = queueService.deleteQueuePatient(queueId);
        return ResponseEntity.ok(new GenericResponse(success, success ? "Xóa thành công" : "Không tìm thấy hàng đợi"));
    }

    @PatchMapping("/{queueId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID queueId, @RequestParam String status) {
        return ResponseEntity.ok(queueService.updateQueueStatus(queueId, status));
    }

    @PutMapping("/{queueId}")
    public ResponseEntity<?> updateQueue(@PathVariable UUID queueId, @RequestBody QueuePatientResponse dto) {
        return ResponseEntity.ok(queueService.updateQueuePatient(queueId, dto));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchQueue(@RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String status) {
        return ResponseEntity.ok(queueService.searchQueue(phone, status));
    }

    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedQueues() {
        return ResponseEntity.ok(queueService.getCompletedQueues());
    }
}