package com.example.demo.service;

import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.QueueRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final PatientRepository patientRepository;

    public QueueService(QueueRepository queueRepository, PatientRepository patientRepository) {
        this.queueRepository = queueRepository;
        this.patientRepository = patientRepository;
    }
    // --- Lấy chi tiết một hàng đợi theo ID ---
    public Optional<QueuePatientResponse> getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .map(this::mapToDto);
    }

    // --- Lấy danh sách đang chờ ---
    public List<QueuePatientResponse> getWaitingPatients() {
        List<Queue> queueList = queueRepository.findByStatusOrderByCheckInTimeAsc(Queue.QueueStatus.Waiting);

        return queueList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Thêm mới ---


    public String generateNextQueueNumber() {
        String prefix = "A";

        Queue last = queueRepository.findTopByOrderByCreatedAtDesc();

        if (last == null || last.getQueueNumber() == null) {
            return prefix + "001";
        }

        String lastNum = last.getQueueNumber().substring(1);
        int next = Integer.parseInt(lastNum) + 1;

        return prefix + String.format("%03d", next);
    }

    public QueuePatientResponse addQueuePatient(QueuePatientResponse dto) {

        Patient newPatient = Patient.builder()
                .fullName(dto.getPatientName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .dob(dto.getDob())
                .gender(mapGender(dto.getGender()))
                .address(dto.getAddress())
                .build();

        Patient savedPatient = patientRepository.save(newPatient);

        Queue queue = Queue.builder()
                .queueNumber(generateNextQueueNumber()) // ✔ auto generate
                .priority(dto.getPriority() != null ? Queue.Priority.valueOf(dto.getPriority()) : Queue.Priority.Normal)
                .status(Queue.QueueStatus.Waiting) // ✔ FE chỉ cho chọn "Chờ khám"
                .checkInTime(dto.getCheckInTime() != null
                        ? LocalDateTime.parse(dto.getCheckInTime())
                        : LocalDateTime.now())
                .patient(savedPatient)
                .build();

        Queue savedQueue = queueRepository.save(queue);

        return mapToDto(savedQueue);
    }

    // helper: map gender string -> Patient.Gender an toàn (chấp nhận "Male", "male", "MALE" ...)
    private Patient.Gender mapGender(String genderStr) {
        if (genderStr == null) return Patient.Gender.other;
        try {
            // enum values in Patient are: male, female, other (lowercase)
            return Patient.Gender.valueOf(genderStr.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            return Patient.Gender.other;
        }
    }

    // --- Xoá ---
    public boolean deleteQueuePatient(UUID queueId) {
        Optional<Queue> queueOpt = queueRepository.findById(queueId);
        if (queueOpt.isPresent()) {
            queueRepository.deleteById(queueId);
            return true;
        }
        return false;
    }

    // --- Sửa trạng thái nhanh ---
    public QueuePatientResponse updateQueueStatus(UUID queueId, String status) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Hàng đợi không tồn tại"));
        queue.setStatus(Queue.QueueStatus.valueOf(status));
        queueRepository.save(queue);

        return mapToDto(queue);
    }

    // --- Sửa toàn bộ thông tin ---
    public QueuePatientResponse updateQueuePatient(UUID queueId, QueuePatientResponse dto) {

        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Hàng đợi không tồn tại"));

        // --- Update queue info ---
        // ❌ KHÔNG SET queueNumber - giữ nguyên giá trị cũ
        // queue.setQueueNumber(dto.getQueueNumber()); // XÓA DÒNG NÀY!

        // ✅ Chỉ update các field cần thiết
        if (dto.getPriority() != null) {
            queue.setPriority(Queue.Priority.valueOf(dto.getPriority()));
        }

        if (dto.getStatus() != null) {
            queue.setStatus(Queue.QueueStatus.valueOf(dto.getStatus()));
        }

        if (dto.getCheckInTime() != null) {
            queue.setCheckInTime(LocalDateTime.parse(dto.getCheckInTime()));
        }

        // --- Update patient info ---
        Patient patient = queue.getPatient();
        if (patient != null) {
            if (dto.getPatientName() != null) patient.setFullName(dto.getPatientName());
            if (dto.getPhone() != null) patient.setPhone(dto.getPhone());
            if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
            if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
            if (dto.getDob() != null) patient.setDob(dto.getDob());

            // Gender phải convert String -> Enum
            if (dto.getGender() != null) {
                try {
                    patient.setGender(Patient.Gender.valueOf(dto.getGender().toLowerCase()));
                } catch (IllegalArgumentException e) {
                    patient.setGender(Patient.Gender.other);
                }
            }
        }

        queueRepository.save(queue);

        return mapToDto(queue);
    }


    // --- Tìm kiếm ---
    public List<QueuePatientResponse> searchQueue(String phone, String status) {
        List<Queue> list = queueRepository.findAll();

        return list.stream()
                .filter(q -> (phone == null || (q.getPatient() != null && q.getPatient().getPhone().contains(phone))) &&
                        (status == null || q.getStatus().name().equals(status)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Helper map entity -> DTO ---
    public QueuePatientResponse mapToDto(Queue queue) {
        Patient p = queue.getPatient();

        return QueuePatientResponse.builder()
                .queueId(queue.getQueueId())
                .queueNumber(queue.getQueueNumber())
                .priority(queue.getPriority().name())
                .status(queue.getStatus().name())
                .checkInTime(queue.getCheckInTime() != null
                        ? queue.getCheckInTime().toString()
                        : null)

                // patient info
                .patientId(p != null ? p.getPatientId() : null)
                .patientName(p != null ? p.getFullName() : null)
                .phone(p != null ? p.getPhone() : null)
                .email(p != null ? p.getEmail() : null)
                .dob(p != null ? p.getDob() : null)
                .gender(p != null ? p.getGender().name() : null)
                .address(p != null ? p.getAddress() : null)

                .build();
    }

}
//.
