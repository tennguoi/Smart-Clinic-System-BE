package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CurrentPatientDTO;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.RoomResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.repository.UserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class CurrentPatientController {
    private final ExaminationRoomRepository examinationRoomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final UserRepository userRepository;
    private final RoomService roomService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
    @PostMapping("/complete-examination")
    @Transactional
    public ResponseEntity<ApiResponse> completeCurrentExamination() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không xác định được bác sĩ"));
        }

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám của bác sĩ"));

        ApiResponse<RoomResponse> result = roomService.completeExamination(room.getRoomId());

        return ResponseEntity.ok(result);
    }
    @PatchMapping("/queue/{queueId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID queueId, @RequestParam String status) {
        return ResponseEntity.ok(queueService.updateQueueStatus(queueId, status));
    }
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedQueues() {
        return  ResponseEntity.ok(queueService.getCompletedQueues());
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

        // Sử dụng query với eager fetch để tránh LazyInitializationException
        Queue queue = queueRepository.findByIdWithPatientAndRoom(room.getCurrentQueueId())
                .orElse(null);

        if (queue == null || queue.getAssignedRoom() == null || !queue.getAssignedRoom().getRoomId().equals(room.getRoomId())) {
            return ResponseEntity.ok(null);
        }

        Patient patient = queue.getPatient();
        
        if (patient == null) {
            return ResponseEntity.ok(null);
        }

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
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }
        
        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Queue> waitingQueues = queueRepository.findWaitingQueuesByRoomIdOrderedByPriority(room.getRoomId());

        List<QueuePatientResponse> response = waitingQueues.stream()
                .map(queueService::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/queue/all")
    public ResponseEntity<List<QueuePatientResponse>> getAllAssignedPatients() {
        User doctor = getCurrentUser();
        if (doctor == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Tìm phòng active của bác sĩ
        ExaminationRoom room = examinationRoomRepository
                .findByDoctorIdAndIsActiveTrue(doctor.getUserId())
                .orElse(null);

        if (room == null) {
            // Nếu không tìm thấy phòng, trả về danh sách rỗng
            return ResponseEntity.ok(List.of());
        }

        // Lấy tất cả bệnh nhân đã được phân vào phòng này (status = InProgress hoặc Waiting)
        List<Queue> assignedQueues = queueRepository.findAssignedQueuesByRoomIdOrderedByPriority(room.getRoomId());

        List<QueuePatientResponse> response = assignedQueues.stream()
                .map(queueService::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
