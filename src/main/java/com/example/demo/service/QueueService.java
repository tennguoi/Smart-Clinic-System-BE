package com.example.demo.service;

import com.example.demo.dto.QueueRequest;
import com.example.demo.dto.QueueResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ExaminationRoomRepository examinationRoomRepository;

    @Transactional
    public QueueResponse checkIn(QueueRequest request) {
        // Get patient
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Get appointment if provided
        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        }

        // Generate queue number (format: A001, A002, ...)
        String queueNumber = generateQueueNumber();

        // Determine priority
        Queue.Priority priority = Queue.Priority.Normal;
        if (request.priority() != null) {
            try {
                priority = Queue.Priority.valueOf(request.priority());
            } catch (IllegalArgumentException e) {
                priority = Queue.Priority.Normal;
            }
        }

        // Create queue entry
        Queue queue = Queue.builder()
                .patient(patient)
                .appointment(appointment)
                .queueNumber(queueNumber)
                .status(Queue.Status.Waiting)
                .priority(priority)
                .checkInTime(LocalDateTime.now())
                .build();

        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    @Transactional
    public QueueResponse assignRoom(UUID queueId, UUID roomId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("Queue not found"));

        if (queue.getStatus() != Queue.Status.Waiting) {
            throw new IllegalArgumentException("Only waiting queues can be assigned to rooms");
        }

        ExaminationRoom room = examinationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.getStatus() != ExaminationRoom.Status.Available) {
            throw new IllegalArgumentException("Room is not available");
        }

        // Update queue
        queue.setAssignedRoom(room);
        queue.setStatus(Queue.Status.InProgress);
        queue.setStartTime(LocalDateTime.now());

        // Update room
        room.setCurrentQueue(queue);
        room.setStatus(ExaminationRoom.Status.Occupied);

        queueRepository.save(queue);
        examinationRoomRepository.save(room);

        return toResponse(queue);
    }

    @Transactional
    public QueueResponse completeExamination(UUID queueId, UUID medicalRecordId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("Queue not found"));

        if (queue.getStatus() != Queue.Status.InProgress) {
            throw new IllegalArgumentException("Only in-progress queues can be completed");
        }

        // Validate: Phải có medical record trước khi hoàn thành
        if (medicalRecordId == null) {
            throw new IllegalArgumentException("Vui lòng tạo hồ sơ bệnh án trước khi hoàn thành");
        }

        // Update queue
        queue.setStatus(Queue.Status.Completed);
        queue.setEndTime(LocalDateTime.now());

        // Update room
        if (queue.getAssignedRoom() != null) {
            ExaminationRoom room = queue.getAssignedRoom();
            room.setCurrentQueue(null);
            room.setStatus(ExaminationRoom.Status.Available);
            examinationRoomRepository.save(room);
        }

        queue = queueRepository.save(queue);
        return toResponse(queue);
    }

    public List<QueueResponse> getWaitingQueues() {
        return queueRepository.findByStatusOrderByPriorityAndCheckInTime(Queue.Status.Waiting)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<QueueResponse> getInProgressQueues() {
        return queueRepository.findByStatusOrderByCheckInTimeAsc(Queue.Status.InProgress)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QueueResponse getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Queue not found"));
    }

    private String generateQueueNumber() {
        // Format: A001, A002, ... Z999
        String prefix = "A";
        int sequence = 1;

        // Find the last queue number for today
        List<Queue> todayQueues = queueRepository.findAll().stream()
                .filter(q -> q.getCheckInTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .collect(Collectors.toList());

        if (!todayQueues.isEmpty()) {
            // Get the highest sequence number
            int maxSeq = todayQueues.stream()
                    .mapToInt(q -> {
                        try {
                            String num = q.getQueueNumber().substring(1);
                            return Integer.parseInt(num);
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            sequence = maxSeq + 1;
        }

        // If sequence exceeds 999, move to next prefix
        if (sequence > 999) {
            prefix = "B";
            sequence = 1;
        }

        return String.format("%s%03d", prefix, sequence);
    }

    private QueueResponse toResponse(Queue queue) {
        return new QueueResponse(
                queue.getQueueId(),
                queue.getPatient().getPatientId(),
                queue.getPatient().getFullName(),
                queue.getPatient().getPatientCode(),
                queue.getAssignedRoom() != null ? queue.getAssignedRoom().getRoomId() : null,
                queue.getAssignedRoom() != null ? queue.getAssignedRoom().getRoomNumber() : null,
                queue.getAppointment() != null ? queue.getAppointment().getAppointmentId() : null,
                queue.getAppointment() != null ? queue.getAppointment().getAppointmentCode() : null,
                queue.getQueueNumber(),
                queue.getStatus(),
                queue.getPriority(),
                queue.getCheckInTime(),
                queue.getStartTime(),
                queue.getEndTime(),
                queue.getCreatedAt()
        );
    }
}

