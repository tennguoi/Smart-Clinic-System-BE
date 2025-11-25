package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.billing.BillingResponseDTO;
import com.example.demo.dto.prescription.AddPrescriptionToExaminationRequest;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MedicalRecordService;
import com.example.demo.service.QueueService;
import com.example.demo.service.RoomService;
import com.example.demo.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class CurrentPatientController {

    private final ExaminationRoomRepository examinationRoomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final UserRepository userRepository;
    private final RoomService roomService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    // ================== API MỚI: LẤY DANH SÁCH BỆNH NHÂN CHỜ CỦA PHÒNG BÁC SĨ ==================

    /**
     * Lấy danh sách bệnh nhân đang chờ trong phòng của bác sĩ hiện tại
     * GET /api/doctor/queue/waiting
     */
    @GetMapping("/queue/waiting")
    public ResponseEntity<List<QueuePatientResponse>> getWaitingPatients() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null) {
            return ResponseEntity.ok(List.of());
        }

        // Lấy danh sách bệnh nhân chờ trong phòng này
        List<Queue> waitingQueues = queueRepository
                .findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
                        room.getRoomId(),
                        Queue.QueueStatus.Waiting
                );

        List<QueuePatientResponse> response = waitingQueues.stream()
                .map(queueService::mapToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả bệnh nhân được phân vào phòng (cả Waiting và InProgress)
     * GET /api/doctor/queue/all
     */
    @GetMapping("/queue/all")
    public ResponseEntity<List<QueuePatientResponse>> getAllAssignedPatients() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Queue> queues = queueRepository
                .findAssignedQueuesByRoomIdOrderedByPriority(room.getRoomId());

        return ResponseEntity.ok(queues.stream().map(queueService::mapToDto).toList());
    }

    // ================== API CŨ - GIỮ NGUYÊN ==================

    @PostMapping("/complete-examination")
    @Transactional
    public ResponseEntity<ApiResponse> completeCurrentExamination() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không xác định được bác sĩ"));
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        ApiResponse<RoomResponse> result = roomService.completeExamination(room.getRoomId());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/queue/{queueId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID queueId, @RequestParam String status) {
        return ResponseEntity.ok(queueService.updateQueueStatus(queueId, status));
    }

    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedQueues() {
        return ResponseEntity.ok(queueService.getCompletedQueues());
    }

    @GetMapping("/current-patient")
    public ResponseEntity<CurrentPatientDTO> getCurrentPatient() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.ok(null);
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null || room.getCurrentQueueId() == null) {
            return ResponseEntity.ok(null);
        }

        Queue queue = queueRepository.findByIdWithPatientAndRoom(room.getCurrentQueueId()).orElse(null);
        if (queue == null || queue.getPatient() == null) {
            return ResponseEntity.ok(null);
        }

        Patient patient = queue.getPatient();
        CurrentPatientDTO dto = CurrentPatientDTO.builder()
                .queueId(queue.getQueueId())
                .patientId(patient.getPatientId())
                .queueNumber(queue.getQueueNumber())
                .fullName(patient.getFullName())
                .phone(patient.getPhone())
                .dob(patient.getDob())
                .gender(patient.getGender() != null ? patient.getGender().getLabel() : null)
                .priority(queue.getPriority() != null ? queue.getPriority().getLabel() : "Bình thường")
                .checkInTime(queue.getCheckInTime())
                .startTime(queue.getStartTime())
                .build();
        return ResponseEntity.ok(dto);
    }

    // ================== API KHÁM BỆNH ==================

    @PostMapping("/services")
    public ResponseEntity<?> addService(@RequestBody AddServiceToExaminationRequest request) {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            ExaminationSummaryResponse summary = medicalRecordService.addServiceToCurrentExamination(
                    request.currentQueueId(),
                    request.serviceId(),
                    request.quantity(),
                    request.note(),
                    doctor
            );
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/prescription")
    public ResponseEntity<?> addPrescription(@RequestBody AddPrescriptionToExaminationRequest request) {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            ExaminationSummaryResponse summary = medicalRecordService.addPrescriptionToCurrentExamination(
                    request.currentQueueId(),
                    request.drugs(),
                    request.instructions(),
                    doctor
            );
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/examination-summary")
    public ResponseEntity<?> getExaminationSummary() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.ok(null);
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null || room.getCurrentQueueId() == null) {
            return ResponseEntity.ok(null);
        }

        try {
            ExaminationSummaryResponse summary = medicalRecordService
                    .getCurrentExaminationSummary(room.getCurrentQueueId(), doctor);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/create-invoice")
    public ResponseEntity<?> createInvoiceFromCurrentExamination() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null || room.getCurrentQueueId() == null) {
            return ResponseEntity.badRequest().body("Không có bệnh nhân đang khám");
        }

        try {
            ExaminationSummaryResponse summary = medicalRecordService
                    .getCurrentExaminationSummary(room.getCurrentQueueId(), doctor);

            if (summary.services() == null || summary.services().isEmpty()) {
                return ResponseEntity.badRequest().body("Chưa chỉ định dịch vụ nào để tạo hóa đơn");
            }

            BillingResponseDTO invoice = billingService.createInvoiceFromExaminationSummary(summary, doctor);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================== API GỌI BỆNH NHÂN TIẾP THEO ==================

    /**
     * Gọi bệnh nhân tiếp theo trong phòng
     * POST /api/doctor/rooms/call-next
     */
    @PostMapping("/rooms/call-next")
    public ResponseEntity<?> callNextPatient() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        try {
            ApiResponse<QueuePatientResponse> result = roomService.callNextPatient(room.getRoomId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}