package com.example.demo.service;

import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.entity.*;
import com.example.demo.entity.Queue;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final PatientRepository patientRepository;
    private final ExaminationRoomRepository examinationRoomRepository;

    // =================================================================
    // LẤY DỮ LIỆU
    // =================================================================

    public Optional<QueuePatientResponse> getQueueById(UUID queueId) {
        return queueRepository.findById(queueId).map(this::mapToDto);
    }

    /** Danh sách chờ chung – lễ tân thấy tất cả (tương thích controller cũ) */
    public List<QueuePatientResponse> getWaitingPatients() {
        return getAllWaitingPatients();
    }

    /** Danh sách chờ theo phòng – bác sĩ chỉ thấy bệnh nhân của mình */
    public List<QueuePatientResponse> getWaitingPatientsByRoom(UUID roomId) {
        return queueRepository.findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
                roomId, Queue.QueueStatus.Waiting
        ).stream().map(this::mapToDto).toList();
    }

    /** Danh sách chờ chung – lễ tân thấy tất cả (method chính) */
    public List<QueuePatientResponse> getAllWaitingPatients() {
        return queueRepository.findByStatusInOrderByPriorityAscCheckInTimeAsc(
                List.of(Queue.QueueStatus.Waiting, Queue.QueueStatus.InProgress)
        ).stream().map(this::mapToDto).toList();
    }

    /** Danh sách unassigned (giữ lại cho tương thích, nhưng trả về rỗng vì logic mới không có unassigned) */
    public List<QueuePatientResponse> getUnassignedWaitingPatients() {
        return List.of(); // Không còn unassigned queues
    }

    public List<QueuePatientResponse> getCompletedQueues() {
        return queueRepository.findByStatusOrderByEndTimeDesc(Queue.QueueStatus.Completed)
                .stream().map(this::mapToDto).toList();
    }

    // =================================================================
    // THÊM BỆNH NHÂN MỚI – TỰ ĐỘNG PHÂN PHÒNG
    // =================================================================

    @Transactional
    public QueuePatientResponse addQueuePatient(QueuePatientResponse dto) {
        // 1. Tìm hoặc tạo bệnh nhân
        Patient patient = patientRepository.findByPhone(dto.getPhone())
                .orElseGet(() -> createNewPatient(dto));

        // 2. Tạo Queue
        Queue queue = Queue.builder()
                .queueNumber(generateNextQueueNumber())
                .patient(patient)
                .priority(Queue.Priority.valueOf(dto.getPriority() != null ? dto.getPriority() : "Normal"))
                .status(Queue.QueueStatus.Waiting)
                .checkInTime(LocalDateTime.now())
                .build();

        Queue savedQueue = queueRepository.save(queue);

        // 3. TỰ ĐỘNG GÁN VÀO PHÒNG TỐT NHẤT
        ExaminationRoom bestRoom = findBestRoomForAssignment();
        if (bestRoom != null) {
            savedQueue.setAssignedRoom(bestRoom);
            queueRepository.save(savedQueue);

            // CHỈ GỌI LUÔN KHI PHÒNG TRỐNG HOÀN TOÀN
            long waitingInRoom = queueRepository.countByAssignedRoomAndStatus(bestRoom, Queue.QueueStatus.Waiting);

            if (bestRoom.getCurrentQueueId() == null && waitingInRoom == 0) {
                startExamination(savedQueue, bestRoom);
            }
            // Ngược lại: đã có người chờ → để vào hàng chờ bình thường
        }

        return mapToDto(savedQueue);
    }

    private Patient createNewPatient(QueuePatientResponse dto) {
        Patient p = new Patient();
        p.setFullName(dto.getPatientName());
        p.setPhone(dto.getPhone());
        p.setEmail(dto.getEmail());
        p.setAddress(dto.getAddress());
        p.setDob(dto.getDob());
        p.setIdNumber(dto.getIdNumber());
        p.setInsuranceNumber(dto.getInsuranceNumber());
        p.setNotes(dto.getNotes());
        p.setGender(safeParseGender(dto.getGender()));
        return patientRepository.save(p);
    }

    private Patient.Gender safeParseGender(String gender) {
        if (gender == null || gender.isBlank()) return Patient.Gender.other;
        try {
            return Patient.Gender.valueOf(gender.trim().toLowerCase());
        } catch (Exception e) {
            return Patient.Gender.other;
        }
    }

    // =================================================================
    // TỰ ĐỘNG TÌM PHÒNG TỐT NHẤT (ít bệnh nhân chờ nhất)
    // =================================================================

    private ExaminationRoom findBestRoomForAssignment() {
        List<ExaminationRoom> activeRooms = examinationRoomRepository
                .findByIsActiveTrueAndDoctorNotNullOrderByRoomNameAsc();

        if (activeRooms.isEmpty()) return null;

        return activeRooms.stream()
                .min(Comparator.comparingLong(room ->
                        queueRepository.countByAssignedRoomAndStatus(room, Queue.QueueStatus.Waiting)
                ))
                .orElse(null);
    }

    // =================================================================
    // BÁC SĨ GỌI BỆNH NHÂN TIẾP THEO
    // =================================================================

    @Transactional
    public QueuePatientResponse callNextPatient(UUID roomId) {
        ExaminationRoom room = examinationRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() != null) {
            completeCurrentPatient(room);
        }

        List<Queue> waiting = queueRepository.findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
                roomId, Queue.QueueStatus.Waiting);

        if (waiting.isEmpty()) {
            throw new RuntimeException("Không còn bệnh nhân chờ");
        }

        Queue next = waiting.get(0);
        startExamination(next, room);
        return mapToDto(next);
    }

    private void startExamination(Queue queue, ExaminationRoom room) {
        queue.setStatus(Queue.QueueStatus.InProgress);
        queue.setStartTime(LocalDateTime.now());
        room.setCurrentQueueId(queue.getQueueId());
        room.setStatus(ExaminationRoom.RoomStatus.Occupied);

        queueRepository.save(queue);
        examinationRoomRepository.save(room);
    }

    private void completeCurrentPatient(ExaminationRoom room) {
        Queue current = queueRepository.findById(room.getCurrentQueueId()).orElse(null);
        if (current != null) {
            current.setStatus(Queue.QueueStatus.Completed);
            current.setEndTime(LocalDateTime.now());
            queueRepository.save(current);
        }

        room.setCurrentQueueId(null);
        room.setStatus(ExaminationRoom.RoomStatus.Available);
        examinationRoomRepository.save(room);
    }

    // =================================================================
    // CẬP NHẬT TRẠNG THÁI / XÓA / TÌM KIẾM
    // =================================================================

    @Transactional
    public QueuePatientResponse updateQueueStatus(UUID queueId, String status) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng đợi"));

        Queue.QueueStatus newStatus = Queue.QueueStatus.valueOf(status);
        queue.setStatus(newStatus);
        queueRepository.save(queue);

        if (newStatus == Queue.QueueStatus.Completed) {
            ExaminationRoom room = queue.getAssignedRoom();
            if (room != null && queueId.equals(room.getCurrentQueueId())) {
                room.setCurrentQueueId(null);
                room.setStatus(ExaminationRoom.RoomStatus.Available);
                examinationRoomRepository.save(room);
            }
        }

        return mapToDto(queue);
    }

    @Transactional
    public boolean deleteQueuePatient(UUID queueId) {
        return queueRepository.findById(queueId)
                .map(queue -> {
                    queueRepository.delete(queue);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public QueuePatientResponse updateQueuePatient(UUID queueId, QueuePatientResponse dto) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng đợi"));

        if (dto.getPriority() != null) {
            queue.setPriority(Queue.Priority.valueOf(dto.getPriority()));
        }

        Patient patient = queue.getPatient();
        Optional.ofNullable(dto.getPatientName()).ifPresent(patient::setFullName);
        Optional.ofNullable(dto.getPhone()).ifPresent(patient::setPhone);
        Optional.ofNullable(dto.getEmail()).ifPresent(patient::setEmail);
        Optional.ofNullable(dto.getAddress()).ifPresent(patient::setAddress);
        Optional.ofNullable(dto.getDob()).ifPresent(patient::setDob);
        Optional.ofNullable(dto.getGender()).ifPresent(g -> patient.setGender(safeParseGender(g)));
        Optional.ofNullable(dto.getNotes()).ifPresent(patient::setNotes);
        Optional.ofNullable(dto.getIdNumber()).ifPresent(patient::setIdNumber);
        Optional.ofNullable(dto.getInsuranceNumber()).ifPresent(patient::setInsuranceNumber);

        patientRepository.save(patient);
        return mapToDto(queue);
    }

    public List<QueuePatientResponse> searchQueue(String phone, String status) {
        return queueRepository.findAll().stream()
                .filter(q -> phone == null || (q.getPatient() != null && q.getPatient().getPhone().contains(phone)))
                .filter(q -> status == null || q.getStatus().name().equalsIgnoreCase(status))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // =================================================================
    // UTILS
    // =================================================================

    private String generateNextQueueNumber() {
        Queue last = queueRepository.findTopByOrderByCreatedAtDesc();
        int next = (last != null && last.getQueueNumber() != null)
                ? Integer.parseInt(last.getQueueNumber().substring(1)) + 1
                : 1;
        return "A" + String.format("%03d", next);
    }

    public QueuePatientResponse mapToDto(Queue queue) {
        Patient p = queue.getPatient();
        ExaminationRoom room = queue.getAssignedRoom();

        var builder = QueuePatientResponse.builder()
                .queueId(queue.getQueueId())
                .queueNumber(queue.getQueueNumber())
                .priority(queue.getPriority() != null ? queue.getPriority().getLabel() : "Normal")
                .status(queue.getStatus() != null ? queue.getStatus().getLabel() : "Chờ khám")
                .checkInTime(queue.getCheckInTime() != null ? queue.getCheckInTime().toString() : null)
                .patientId(p.getPatientId())
                .patientName(p.getFullName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .dob(p.getDob())
                .gender(p.getGender() != null ? p.getGender().getLabel() : null)
                .address(p.getAddress())
                .idNumber(p.getIdNumber())
                .insuranceNumber(p.getInsuranceNumber())
                .notes(p.getNotes());

        if (room != null) {
            builder.roomId(room.getRoomId())
                    .roomName(room.getRoomName())
                    .doctorName(room.getDoctor() != null ? room.getDoctor().getFullName() : null);
        }

        return builder.build();
    }
}