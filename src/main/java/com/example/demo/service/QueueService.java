package com.example.demo.service;

import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.appointment.AppointmentCheckInDTO; // ✅ IMPORT ĐÚNG
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
    private final AppointmentRepository appointmentRepository;

    // =================================================================
    // ✨ TÍNH NĂNG MỚI: TẠO HÀNG CHỜ TỪ LỊCH HẸN
    // =================================================================

    /**
     * Tạo hàng chờ từ lịch hẹn - Tự động điền thông tin và ưu tiên
     * @param appointmentId ID của lịch hẹn
     * @return Thông tin bệnh nhân trong hàng chờ
     */
    @Transactional
    public QueuePatientResponse createQueueFromAppointment(UUID appointmentId) {
        // 1. Lấy thông tin appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        // 2. Kiểm tra trạng thái appointment
        if (appointment.getStatus() != Appointment.Status.Confirmed) {
            throw new RuntimeException("Lịch hẹn chưa được xác nhận");
        }

        // 3. Kiểm tra xem đã tạo queue cho appointment này chưa
        Optional<Queue> existingQueue = queueRepository.findByAppointmentId(appointmentId);
        if (existingQueue.isPresent()) {
            throw new RuntimeException("Lịch hẹn này đã được tạo hàng chờ");
        }

        // 4. Tìm hoặc tạo bệnh nhân từ thông tin appointment
        Patient patient = patientRepository.findByPhone(appointment.getPhone())
                .orElseGet(() -> createPatientFromAppointment(appointment));

        // 5. Tạo Queue với ưu tiên cao cho người đặt lịch
        Queue queue = Queue.builder()
                .queueNumber(generateNextQueueNumber())
                .patient(patient)
                .appointment(appointment)
                .priority(Queue.Priority.Urgent)  // ✨ Ưu tiên cho người đặt lịch
                .status(Queue.QueueStatus.Waiting)
                .checkInTime(LocalDateTime.now())
                .build();

        Queue savedQueue = queueRepository.save(queue);

        // 6. Tự động phân phòng tốt nhất
        ExaminationRoom bestRoom = findBestRoomForAssignment();
        if (bestRoom != null) {
            savedQueue.setAssignedRoom(bestRoom);
            queueRepository.save(savedQueue);

            // Nếu phòng trống hoàn toàn, bắt đầu khám luôn
            long waitingInRoom = queueRepository.countByAssignedRoomAndStatus(
                    bestRoom, Queue.QueueStatus.Waiting
            );

            if (bestRoom.getCurrentQueueId() == null && waitingInRoom == 0) {
                startExamination(savedQueue, bestRoom);
            }
        }

        return mapToDto(savedQueue);
    }

    /**
     * Tạo bệnh nhân mới từ thông tin appointment
     */
    private Patient createPatientFromAppointment(Appointment appointment) {
        Patient patient = new Patient();
        patient.setFullName(appointment.getPatientName());
        patient.setPhone(appointment.getPhone());
        patient.setEmail(appointment.getEmail());
        patient.setNotes(appointment.getNotes());

        // Giới tính và ngày sinh cần được bổ sung sau (hoặc yêu cầu nhập khi check-in)
        patient.setGender(Patient.Gender.other);
        patient.setDob(java.time.LocalDate.now().minusYears(30)); // Giá trị mặc định

        return patientRepository.save(patient);
    }

    /**
     * Lấy danh sách lịch hẹn đã xác nhận trong ngày hôm nay
     * Dùng để hiển thị cho lễ tân danh sách người cần check-in
     */
    public List<AppointmentCheckInDTO> getTodayConfirmedAppointments() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Appointment> appointments = appointmentRepository
                .findByStatusAndAppointmentTimeBetween(
                        Appointment.Status.Confirmed,
                        startOfDay,
                        endOfDay
                );

        return appointments.stream()
                .map(apt -> {
                    // Kiểm tra xem đã tạo queue chưa
                    boolean hasQueue = queueRepository.findByAppointmentId(apt.getAppointmentId()).isPresent();

                    return AppointmentCheckInDTO.builder()
                            .appointmentId(apt.getAppointmentId())
                            .appointmentCode(apt.getAppointmentCode())
                            .patientName(apt.getPatientName())
                            .phone(apt.getPhone())
                            .email(apt.getEmail())
                            .appointmentTime(apt.getAppointmentTime())
                            .notes(apt.getNotes())
                            .hasCheckedIn(hasQueue)
                            .build();
                })
                .sorted(Comparator.comparing(AppointmentCheckInDTO::getAppointmentTime))
                .collect(Collectors.toList());
    }

    // =================================================================
    // THÊM BỆNH NHÂN MỚI - CẢI TIẾN
    // =================================================================

    @Transactional
    public QueuePatientResponse addQueuePatient(QueuePatientResponse dto) {
        // 1. Tìm hoặc tạo bệnh nhân
        Patient patient = patientRepository.findByPhone(dto.getPhone())
                .orElseGet(() -> createNewPatient(dto));

        // 2. Xác định mức độ ưu tiên
        Queue.Priority priority = determinePriority(dto.getPriority());

        // 3. Tạo Queue
        Queue queue = Queue.builder()
                .queueNumber(generateNextQueueNumber())
                .patient(patient)
                .priority(priority)
                .status(Queue.QueueStatus.Waiting)
                .checkInTime(LocalDateTime.now())
                .build();

        Queue savedQueue = queueRepository.save(queue);

        // 4. Tự động phân phòng tốt nhất
        ExaminationRoom bestRoom = findBestRoomForAssignment();
        if (bestRoom != null) {
            savedQueue.setAssignedRoom(bestRoom);
            queueRepository.save(savedQueue);

            long waitingInRoom = queueRepository.countByAssignedRoomAndStatus(
                    bestRoom, Queue.QueueStatus.Waiting
            );

            if (bestRoom.getCurrentQueueId() == null && waitingInRoom == 0) {
                startExamination(savedQueue, bestRoom);
            }
        }

        return mapToDto(savedQueue);
    }

    /**
     * Xác định mức độ ưu tiên dựa trên input
     */
    private Queue.Priority determinePriority(String priorityStr) {
        if (priorityStr == null || priorityStr.isBlank()) {
            return Queue.Priority.Normal;
        }

        try {
            return Queue.Priority.valueOf(priorityStr);
        } catch (Exception e) {
            return Queue.Priority.Normal;
        }
    }

    // =================================================================
    // LẤY DỮ LIỆU - CẢI TIẾN XẾP HÀNG
    // =================================================================

    /**
     * Danh sách chờ - ƯU TIÊN người đặt lịch trước
     * Sắp xếp theo: Priority (Emergency > Urgent > Normal) → Thời gian check-in
     */
    public List<QueuePatientResponse> getAllWaitingPatients() {
        List<Queue> queues = queueRepository.findByStatusInOrderByPriorityAscCheckInTimeAsc(
                List.of(Queue.QueueStatus.Waiting, Queue.QueueStatus.InProgress)
        );

        // Sắp xếp lại: Urgent (đặt lịch) lên trước Normal (walk-in) cùng thời điểm
        return queues.stream()
                .sorted(this::compareQueuePriority)
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Comparator ưu tiên người đặt lịch
     * Emergency > Urgent (có appointment) > Urgent (không có appointment) > Normal
     */
    private int compareQueuePriority(Queue q1, Queue q2) {
        // 1. So sánh Emergency trước
        if (q1.getPriority() == Queue.Priority.Emergency && q2.getPriority() != Queue.Priority.Emergency) {
            return -1;
        }
        if (q2.getPriority() == Queue.Priority.Emergency && q1.getPriority() != Queue.Priority.Emergency) {
            return 1;
        }

        // 2. Cùng Urgent: có appointment lên trước
        if (q1.getPriority() == Queue.Priority.Urgent && q2.getPriority() == Queue.Priority.Urgent) {
            boolean q1HasAppointment = q1.getAppointment() != null;
            boolean q2HasAppointment = q2.getAppointment() != null;

            if (q1HasAppointment && !q2HasAppointment) return -1;
            if (q2HasAppointment && !q1HasAppointment) return 1;
        }

        // 3. Urgent lên trước Normal
        if (q1.getPriority() == Queue.Priority.Urgent && q2.getPriority() == Queue.Priority.Normal) {
            return -1;
        }
        if (q2.getPriority() == Queue.Priority.Urgent && q1.getPriority() == Queue.Priority.Normal) {
            return 1;
        }

        // 4. Cùng priority: so sánh thời gian check-in
        return q1.getCheckInTime().compareTo(q2.getCheckInTime());
    }

    public List<QueuePatientResponse> getWaitingPatientsByRoom(UUID roomId) {
        List<Queue> queues = queueRepository
                .findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
                        roomId, Queue.QueueStatus.Waiting
                );

        return queues.stream()
                .sorted(this::compareQueuePriority)
                .map(this::mapToDto)
                .toList();
    }

    // =================================================================
    // BÁC SĨ GỌI BỆNH NHÂN TIẾP THEO - CẢI TIẾN
    // =================================================================

    @Transactional
    public QueuePatientResponse callNextPatient(UUID roomId) {
        ExaminationRoom room = examinationRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() != null) {
            completeCurrentPatient(room);
        }

        List<Queue> waiting = queueRepository
                .findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
                        roomId, Queue.QueueStatus.Waiting
                );

        if (waiting.isEmpty()) {
            throw new RuntimeException("Không còn bệnh nhân chờ");
        }

        // Sắp xếp theo priority (ưu tiên người đặt lịch)
        Queue next = waiting.stream()
                .sorted(this::compareQueuePriority)
                .findFirst()
                .orElseThrow();

        startExamination(next, room);
        return mapToDto(next);
    }

    // =================================================================
    // HELPER METHODS (GIỮ NGUYÊN)
    // =================================================================

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
        Appointment appointment = queue.getAppointment();

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
                .notes(p.getNotes())
                .hasAppointment(appointment != null);  // ✨ Đánh dấu có đặt lịch

        if (room != null) {
            builder.roomId(room.getRoomId())
                    .roomName(room.getRoomName())
                    .doctorName(room.getDoctor() != null ? room.getDoctor().getFullName() : null);
        }

        if (appointment != null) {
            builder.appointmentCode(appointment.getAppointmentCode());
        }

        return builder.build();
    }

    // Các phương thức khác giữ nguyên
    public Optional<QueuePatientResponse> getQueueById(UUID queueId) {
        return queueRepository.findById(queueId).map(this::mapToDto);
    }

    public List<QueuePatientResponse> getWaitingPatients() {
        return getAllWaitingPatients();
    }

    public List<QueuePatientResponse> getUnassignedWaitingPatients() {
        return List.of();
    }

    public List<QueuePatientResponse> getCompletedQueues() {
        return queueRepository.findByStatusOrderByEndTimeDesc(Queue.QueueStatus.Completed)
                .stream().map(this::mapToDto).toList();
    }

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
            queue.setPriority(determinePriority(dto.getPriority()));
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
                .sorted(this::compareQueuePriority)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}