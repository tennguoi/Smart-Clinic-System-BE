package com.example.demo.controller;

import com.example.demo.dto.CurrentPatientDTO;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class CurrentPatientController {
    private ExaminationRoomRepository examinationRoomRepository;

    private QueueRepository queueRepository;

    @GetMapping("/current-patient")
    public ResponseEntity<CurrentPatientDTO> getCurrentPatient(@AuthenticationPrincipal User doctor) {

        ExaminationRoom room = examinationRoomRepository
                .findByDoctorAndStatusAndIsActiveTrue(doctor, ExaminationRoom.RoomStatus.Occupied)
                .orElse(null);

        if (room == null || room.getCurrentQueueId() == null) {
            return ResponseEntity.ok(null);
        }

        Queue queue = queueRepository.findById(room.getCurrentQueueId())
                .orElseThrow();

        Patient patient = queue.getPatient();

        CurrentPatientDTO dto = CurrentPatientDTO.builder()
                .queueNumber(queue.getQueueNumber())
                .fullName(patient.getFullName())
                .phone(patient.getPhone())
                .dob(patient.getDob())
                .gender(patient.getGender().getLabel())
                .priority(queue.getPriority() != null ? queue.getPriority().getLabel() : "Bình thường")
                .checkInTime(queue.getCheckInTime())
                .startTime(queue.getStartTime())
                .build();

        return ResponseEntity.ok(dto);
    }
}
