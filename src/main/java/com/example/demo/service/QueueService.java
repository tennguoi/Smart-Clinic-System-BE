package com.example.demo.service;

import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.QueueRepository;
import org.springframework.stereotype.Service;
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
    // Trả về tất cả bệnh nhân đang chờ (bao gồm cả đã phân phòng và chưa phân phòng)
    // Sắp xếp theo priority (Emergency → Urgent → Normal) và checkInTime
    public List<QueuePatientResponse> getWaitingPatients() {
        List<Queue> queueList = queueRepository.findWaitingQueuesOrderedByPriority();

        return queueList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    // --- Lấy danh sách đang chờ và chưa được phân phòng ---
    // Dùng cho lễ tân khi cần phân phòng
    public List<QueuePatientResponse> getUnassignedWaitingPatients() {
        List<Queue> queueList = queueRepository.findUnassignedWaitingQueuesOrderedByPriority();

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
        if (dto.getPatientName() == null || dto.getPatientName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bệnh nhân không được để trống");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        // --- Kiểm tra trùng số điện thoại ---
        if (isPhoneExists(dto.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại, không thể thêm bệnh nhân trùng");
        }

        if (dto.getDob() == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }

        Patient newPatient = Patient.builder()
                .fullName(dto.getPatientName().trim())
                .phone(dto.getPhone().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .dob(dto.getDob())
                .gender(mapGender(dto.getGender()))
                .address(dto.getAddress() != null ? dto.getAddress().trim() : null)
                .build();

        Patient savedPatient = patientRepository.save(newPatient);

        // Xử lý priority
        Queue.Priority priority = Queue.Priority.Normal;
        if (dto.getPriority() != null && !dto.getPriority().trim().isEmpty()) {
            try {
                priority = Queue.Priority.valueOf(dto.getPriority().trim());
            } catch (IllegalArgumentException e) {
                priority = Queue.Priority.Normal;
            }
        }

        LocalDateTime checkInTime = LocalDateTime.now();
        if (dto.getCheckInTime() != null && !dto.getCheckInTime().trim().isEmpty()) {
            try {
                checkInTime = LocalDateTime.parse(dto.getCheckInTime());
            } catch (Exception e) {
                checkInTime = LocalDateTime.now();
            }
        }

        Queue queue = Queue.builder()
                .queueNumber(generateNextQueueNumber())
                .priority(priority)
                .status(Queue.QueueStatus.Waiting)
                .checkInTime(checkInTime)
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
    private boolean isPhoneExists(String phone) {
        return patientRepository.findByPhone(phone.trim()).isPresent();
    }
    // --- Helper map entity -> DTO ---
    public QueuePatientResponse mapToDto(Queue queue) {
        Patient p = queue.getPatient();

        return QueuePatientResponse.builder()
                .queueId(queue.getQueueId())
                .queueNumber(queue.getQueueNumber())
                .priority(queue.getPriority() != null ? queue.getPriority().getLabel() : "Bình thường")
                .status(queue.getStatus() != null ? queue.getStatus().getLabel() : null)
                .checkInTime(queue.getCheckInTime() != null
                        ? queue.getCheckInTime().toString()
                        : null)

                // patient info
                .patientId(p != null ? p.getPatientId() : null)
                .patientName(p != null ? p.getFullName() : null)
                .phone(p != null ? p.getPhone() : null)
                .email(p != null ? p.getEmail() : null)
                .dob(p != null ? p.getDob() : null)
                .gender(p != null && p.getGender() != null ? p.getGender().getLabel() : null)
                .address(p != null ? p.getAddress() : null)

                .build();
    }

}
//.
