package com.example.demo.controller;

import com.example.demo.dto.CurrentPatientDTO;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class CurrentPatientController {
    private final ExaminationRoomRepository examinationRoomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;

    @GetMapping("/current-patient")
    public ResponseEntity<CurrentPatientDTO> getCurrentPatient(@AuthenticationPrincipal User doctor) {
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
    public ResponseEntity<List<QueuePatientResponse>> getWaitingPatients(@AuthenticationPrincipal User doctor) {
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
    public ResponseEntity<List<QueuePatientResponse>> getAllAssignedPatients(@AuthenticationPrincipal User doctor) {
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
