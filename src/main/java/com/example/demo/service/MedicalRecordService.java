package com.example.demo.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import com.example.demo.dto.MedicalRecordSearchCriteria;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import com.example.demo.dto.*;
import com.example.demo.dto.prescription.AddPrescriptionRequest;
import com.example.demo.dto.prescription.PrescriptionResponse;
import com.example.demo.entity.*;
import com.example.demo.entity.MedicalRecordServiceEntity;
import com.example.demo.entity.Patient;
import com.example.demo.repository.*;
import com.example.demo.repository.PatientRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final JwtUtil jwtUtil;
    private final QueueRepository queueRepository;
    private final ServiceRepository serviceRepository;
    private final MedicalRecordServiceRepository medicalRecordServiceRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
            PrescriptionRepository prescriptionRepository,
            UserRepository userRepository,
            PatientRepository patientRepository,
            JwtUtil jwtUtil,
            QueueRepository queueRepository,
            ServiceRepository serviceRepository,
            MedicalRecordServiceRepository medicalRecordServiceRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.jwtUtil = jwtUtil;
        this.queueRepository = queueRepository;
        this.serviceRepository = serviceRepository;
        this.medicalRecordServiceRepository = medicalRecordServiceRepository;
    }

    @Transactional
    public MedicalRecordResponse create(String bearerToken, MedicalRecordRequest request) {
        String token = extractToken(bearerToken);
        UUID doctorId = jwtUtil.getUserIdFromToken(token);
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = null;
        String patientName = null;

        // Ưu tiên lấy từ patientId nếu có
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + request.getPatientId()));
            patientName = patient.getFullName();
        } else if (request.getPatientName() != null && !request.getPatientName().trim().isEmpty()) {
            patientName = request.getPatientName().trim();
        }

        MedicalRecord record = MedicalRecord.builder()
                .doctor(doctor)
                .patient(patient) // Gán patient nếu có
                .patientName(patientName)
                .diagnosis(request.getDiagnosis())
                .treatmentNotes(request.getTreatmentNotes())
                .build();

        record = medicalRecordRepository.save(record);
        return toResponse(record);
    }

    @Transactional
    public void addPrescriptionToRecord(AddPrescriptionRequest request) {
        if (request.getRecordId() == null) {
            throw new IllegalArgumentException("recordId không được để trống");
        }
        if (request.getDrugs() == null || request.getDrugs().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thuốc (drugs) không được để trống");
        }
        if (request.getInstructions() == null || request.getInstructions().trim().isEmpty()) {
            throw new IllegalArgumentException("Hướng dẫn sử dụng (instructions) không được để trống");
        }

        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + request.getRecordId()));

        Optional<Prescription> existingPrescriptionOpt = prescriptionRepository.findByRecordId(request.getRecordId());

        if (existingPrescriptionOpt.isPresent()) {
            Prescription prescription = existingPrescriptionOpt.get();
            String existingDrugs = prescription.getDrugs();
            String newDrugs = request.getDrugs().trim();
            String updatedDrugs = existingDrugs + "\n" + newDrugs;

            String existingInstructions = prescription.getInstructions();
            String newInstructions = request.getInstructions().trim();
            String updatedInstructions = existingInstructions + "\n" + newInstructions;

            prescription.setDrugs(updatedDrugs);
            prescription.setInstructions(updatedInstructions);
            prescriptionRepository.save(prescription);
        } else {
            Prescription prescription = Prescription.builder()
                    .medicalRecord(record)
                    .drugs(request.getDrugs().trim())
                    .instructions(request.getInstructions().trim())
                    .build();
            prescriptionRepository.save(prescription);
        }
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listByPatient(UUID patientId) {
        return medicalRecordRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listMyCreated(String bearerToken) {
        String token = extractToken(bearerToken);
        UUID doctorId = jwtUtil.getUserIdFromToken(token);
        return medicalRecordRepository.findByDoctor_UserIdOrderByCreatedAtDesc(doctorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listAll() {
        return medicalRecordRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> searchMyRecords(MedicalRecordSearchCriteria criteria) {
        Specification<MedicalRecord> spec = buildSearchSpec(criteria);
        return medicalRecordRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientHistorySummaryResponse> getPatientHistorySummary(MedicalRecordSearchCriteria criteria) {
        Specification<MedicalRecord> spec = buildSearchSpec(criteria);
        // Lấy tất cả hồ sơ thỏa mãn điều kiện
        List<MedicalRecord> records = medicalRecordRepository.findAll(spec);

        // Group by PatientID
        // Với bệnh nhân vãng lai (PatientID == null), ta group theo RecordID (mỗi bản
        // ghi 1 dòng)
        java.util.Map<Object, List<MedicalRecord>> grouped = new java.util.HashMap<>();

        for (MedicalRecord r : records) {
            if (r.getPatient() != null) {
                grouped.computeIfAbsent(r.getPatient().getPatientId(), k -> new ArrayList<>()).add(r);
            } else {
                grouped.computeIfAbsent(r.getRecordId(), k -> new ArrayList<>()).add(r);
            }
        }

        List<PatientHistorySummaryResponse> result = new ArrayList<>();

        for (List<MedicalRecord> list : grouped.values()) {
            if (list.isEmpty())
                continue;

            // Tìm lần khám gần nhất
            MedicalRecord latest = list.stream()
                    .max(java.util.Comparator.comparing(MedicalRecord::getCreatedAt))
                    .orElse(list.get(0));

            Patient p = latest.getPatient();

            result.add(PatientHistorySummaryResponse.builder()
                    .patientId(p != null ? p.getPatientId() : null)
                    .patientName(latest.getPatientName()) // Ưu tiên tên trong record nếu patient null, hoặc hiển thị
                                                          // tên tại thời điểm khám
                    .phone(p != null ? p.getPhone() : null)
                    .idNumber(p != null ? p.getIdNumber() : null)
                    .insuranceNumber(p != null ? p.getInsuranceNumber() : null)
                    .email(p != null ? p.getEmail() : null)
                    .visitCount(list.size())
                    .lastVisitDate(latest.getCreatedAt())
                    .build());
        }

        // Sắp xếp theo ngày khám gần nhất giảm dần
        result.sort((a, b) -> b.getLastVisitDate().compareTo(a.getLastVisitDate()));

        return result;
    }

    private Specification<MedicalRecord> buildSearchSpec(MedicalRecordSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Chỉ lấy hồ sơ của Bác sĩ đang request
            if (criteria.getDoctorId() != null) {
                // "doctor" vẫn là User, nên trỏ vào userId
                predicates.add(cb.equal(root.get("doctor").get("userId"), criteria.getDoctorId()));
            }

            // 2. TÌM KIẾM THÔNG MINH (Bao gồm cả Vãng lai & Patient hệ thống)
            // Tìm theo: Tên, SĐT, CCCD, Bảo hiểm y tế, Chẩn đoán
            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String key = criteria.getKeyword().trim().toLowerCase();

                // ✅ SỬA LẠI: Dùng pattern đơn giản nhất bao bọc bởi 2 dấu %
                // Ví dụ: Nhập "du" sẽ thành "%du%", tìm được "Dung", "Dieu", "Minh Du"...
                String pattern = "%" + key + "%";

                // --- KẾT NỐI VỚI BẢNG PATIENT (Dùng LEFT JOIN) ---
                Join<MedicalRecord, Patient> patientJoin = root.join("patient", JoinType.LEFT);

                // A. TÌM KHÁCH VÃNG LAI (Tìm trong cột patientName của MedicalRecord)
                // coalesce để tránh lỗi null, lower để không phân biệt hoa thường
                var tempName = cb.lower(cb.coalesce(root.get("patientName"), ""));
                Predicate matchTempName = cb.like(tempName, pattern);

                // B. TÌM KHÁCH HỆ THỐNG - TÊN (Tìm trong bảng Patient)
                var realName = cb.lower(cb.coalesce(patientJoin.get("fullName"), ""));
                Predicate matchRealName = cb.like(realName, pattern);

                // C. TÌM KHÁCH HỆ THỐNG - SĐT (Tìm trong bảng Patient)
                var phone = cb.coalesce(patientJoin.get("phone"), "");
                Predicate matchPhone = cb.like(phone, pattern);

                // D. TÌM THEO CCCD/CMND (Tìm trong bảng Patient)
                var idNumber = cb.coalesce(patientJoin.get("idNumber"), "");
                Predicate matchIdNumber = cb.like(idNumber, pattern);

                // E. TÌM THEO SỐ BẢO HIỂM Y TẾ (Tìm trong bảng Patient)
                var insuranceNumber = cb.coalesce(patientJoin.get("insuranceNumber"), "");
                Predicate matchInsurance = cb.like(insuranceNumber, pattern);

                // F. TÌM THEO CHẨN ĐOÁN (Tìm trong MedicalRecord)
                var diagnosis = cb.lower(cb.coalesce(root.get("diagnosis"), ""));
                Predicate matchDiagnosis = cb.like(diagnosis, pattern);

                // ===> GỘP TẤT CẢ LẠI BẰNG OR <===
                predicates.add(
                        cb.or(matchTempName, matchRealName, matchPhone, matchIdNumber, matchInsurance, matchDiagnosis));
            }

            // 3. Tìm theo ngày tháng
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getStartDate().atStartOfDay()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getEndDate().atTime(23, 59, 59)));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public MedicalRecordDetailResponse updateRecord(UUID recordId, UpdateMedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + recordId));

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + request.getPatientId()));
            record.setPatient(patient);
            record.setPatientName(patient.getFullName());
        } else if (request.getPatientName() != null && !request.getPatientName().trim().isEmpty()) {
            record.setPatientName(request.getPatientName().trim());
        }

        if (request.getDiagnosis() != null) {
            record.setDiagnosis(request.getDiagnosis());
        }
        if (request.getTreatmentNotes() != null) {
            record.setTreatmentNotes(request.getTreatmentNotes());
        }
        record = medicalRecordRepository.save(record);

        if (request.getPrescription() != null) {
            UpdateMedicalRecordRequest.PrescriptionUpdateRequest prescriptionRequest = request.getPrescription();
            Optional<Prescription> existingPrescriptionOpt = prescriptionRepository.findByRecordId(recordId);

            if (existingPrescriptionOpt.isPresent()) {
                Prescription prescription = existingPrescriptionOpt.get();
                if (prescriptionRequest.getDrugs() != null) {
                    prescription.setDrugs(prescriptionRequest.getDrugs());
                }
                if (prescriptionRequest.getInstructions() != null) {
                    prescription.setInstructions(prescriptionRequest.getInstructions());
                }
                prescriptionRepository.save(prescription);
            } else {
                Prescription newPrescription = Prescription.builder()
                        .medicalRecord(record)
                        .drugs(prescriptionRequest.getDrugs() != null ? prescriptionRequest.getDrugs() : "")
                        .instructions(
                                prescriptionRequest.getInstructions() != null ? prescriptionRequest.getInstructions()
                                        : "")
                        .build();
                prescriptionRepository.save(newPrescription);
            }
        }
        return getRecordDetail(recordId);
    }

    @Transactional(readOnly = true)
    public MedicalRecordDetailResponse getRecordDetail(UUID recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + recordId));

        PrescriptionResponse prescriptionResponse = null;
        Optional<Prescription> prescriptionOpt = prescriptionRepository.findByRecordId(recordId);
        if (prescriptionOpt.isPresent()) {
            Prescription prescription = prescriptionOpt.get();
            prescriptionResponse = PrescriptionResponse.builder()
                    .prescriptionId(prescription.getPrescriptionId())
                    .recordId(record.getRecordId())
                    .drugs(prescription.getDrugs())
                    .instructions(prescription.getInstructions())
                    .issuedAt(prescription.getIssuedAt())
                    .build();
        }

        String finalPatientName = record.getPatientName();
        if ((finalPatientName == null || finalPatientName.trim().isEmpty()) && record.getPatient() != null) {
            try {
                finalPatientName = record.getPatient().getFullName();
            } catch (Exception e) {
                finalPatientName = record.getPatientName();
            }
        }

        return MedicalRecordDetailResponse.builder()
                .recordId(record.getRecordId())
                .patientId(record.getPatient() != null ? record.getPatient().getPatientId() : null)
                .patientName(finalPatientName)
                .doctorId(record.getDoctor() != null ? record.getDoctor().getUserId() : null)
                .doctorName(record.getDoctor() != null ? record.getDoctor().getFullName() : null)
                .diagnosis(record.getDiagnosis())
                .treatmentNotes(record.getTreatmentNotes())
                .createdAt(record.getCreatedAt())
                .prescription(prescriptionResponse)
                .build();
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        String finalPatientName = r.getPatientName();
        if ((finalPatientName == null || finalPatientName.trim().isEmpty()) && r.getPatient() != null) {
            try {
                finalPatientName = r.getPatient().getFullName();
            } catch (Exception e) {
                finalPatientName = r.getPatientName();
            }
        }
        return MedicalRecordResponse.builder()
                .recordId(r.getRecordId())
                .patientId(r.getPatient() != null ? r.getPatient().getPatientId() : null)
                .patientName(finalPatientName)
                .doctorId(r.getDoctor() != null ? r.getDoctor().getUserId() : null)
                .doctorName(r.getDoctor() != null ? r.getDoctor().getFullName() : null)
                .diagnosis(r.getDiagnosis())
                .treatmentNotes(r.getTreatmentNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteRecord(UUID recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + recordId));

        Optional<Prescription> prescriptionOpt = prescriptionRepository.findByRecordId(recordId);
        if (prescriptionOpt.isPresent()) {
            prescriptionRepository.delete(prescriptionOpt.get());
        }
        medicalRecordRepository.delete(record);
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization token missing");
        }
        return bearerToken.substring(7);
    }

    // ============================== CHỨC NĂNG CHO BỆNH NHÂN ĐANG KHÁM
    // ==============================

    private MedicalRecord getOrCreateRecordForCurrentPatient(Queue queue, User doctor) {
        // 1. Tìm hồ sơ bệnh án gắn với lượt khám này (QueueID)
        return medicalRecordRepository.findByQueueId(queue.getQueueId())
                .orElseGet(() -> {
                    // 2. Nếu chưa có, tạo mới
                    Patient patient = queue.getPatient();
                    MedicalRecord newRecord = MedicalRecord.builder()
                            .doctor(doctor)
                            .patient(patient)
                            .patientName(patient.getFullName())
                            .diagnosis("")
                            .treatmentNotes("")
                            .createdAt(LocalDateTime.now())
                            .queueId(queue.getQueueId()) // ✅ LIÊN KẾT VỚI QUEUE
                            .build();
                    return medicalRecordRepository.save(newRecord);
                });
    }

    @Transactional
    public ExaminationSummaryResponse addServiceToCurrentExamination(UUID currentQueueId, String serviceId,
            Integer quantity, String note, User doctor) {
        Queue queue = queueRepository.findById(currentQueueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lượt khám"));

        com.example.demo.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại"));

        MedicalRecord record = getOrCreateRecordForCurrentPatient(queue, doctor);

        MedicalRecordServiceEntity mrs = MedicalRecordServiceEntity.builder()
                .medicalRecord(record)
                .service(service)
                .quantity(quantity != null && quantity > 0 ? quantity : 1)
                .unitPrice(service.getPrice())
                .note(note)
                .build();

        medicalRecordServiceRepository.save(mrs);
        return buildExaminationSummary(record.getRecordId());
    }

    @Transactional
    public ExaminationSummaryResponse addPrescriptionToCurrentExamination(UUID currentQueueId, String drugs,
            String instructions, User doctor) {
        Queue queue = queueRepository.findById(currentQueueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lượt khám"));

        MedicalRecord record = getOrCreateRecordForCurrentPatient(queue, doctor);

        AddPrescriptionRequest req = new AddPrescriptionRequest();
        req.setRecordId(record.getRecordId());
        req.setDrugs(drugs);
        req.setInstructions(instructions);
        addPrescriptionToRecord(req);

        return buildExaminationSummary(record.getRecordId());
    }

    @Transactional(readOnly = true)
    public ExaminationSummaryResponse getCurrentExaminationSummary(UUID currentQueueId, User doctor) {
        Queue queue = queueRepository.findById(currentQueueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lượt khám"));
        MedicalRecord record = getOrCreateRecordForCurrentPatient(queue, doctor);
        return buildExaminationSummary(record.getRecordId());
    }

    private ExaminationSummaryResponse buildExaminationSummary(UUID recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        List<MedicalRecordServiceEntity> services = medicalRecordServiceRepository
                .findAllByMedicalRecord_RecordId(recordId);

        var serviceItems = services.stream()
                .map(ms -> new ExaminationSummaryResponse.ExaminationServiceItem(
                        ms.getService().getServiceId(),
                        ms.getService().getName(),
                        ms.getService().getCategory().getDisplayName(),
                        ms.getQuantity(),
                        ms.getUnitPrice(),
                        ms.getUnitPrice().multiply(BigDecimal.valueOf(ms.getQuantity())),
                        ms.getNote()))
                .toList();

        BigDecimal total = serviceItems.stream()
                .map(ExaminationSummaryResponse.ExaminationServiceItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PrescriptionResponse prescription = null;
        Optional<Prescription> presOpt = prescriptionRepository.findByRecordId(recordId);
        if (presOpt.isPresent()) {
            Prescription p = presOpt.get();
            prescription = PrescriptionResponse.builder()
                    .prescriptionId(p.getPrescriptionId())
                    .recordId(recordId)
                    .drugs(p.getDrugs())
                    .instructions(p.getInstructions())
                    .issuedAt(p.getIssuedAt())
                    .build();
        }

        return new ExaminationSummaryResponse(
                record.getRecordId(),
                record.getPatient() != null ? record.getPatient().getPatientId() : null,
                record.getPatientName(),
                record.getDiagnosis(),
                record.getTreatmentNotes(),
                record.getCreatedAt(),
                prescription,
                serviceItems,
                total);
    }

    // Trong MedicalRecordService.java
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getAllRecords() {
        return medicalRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== TÌM KIẾM LỊCH SỬ KHÁM THEO THÔNG TIN BỆNH NHÂN
    // ====================

    @Transactional(readOnly = true)
    public PatientHistorySearchResponse searchPatientHistory(String phone, String idNumber, String insuranceNumber) {
        List<MedicalRecord> records = null;
        Patient patient = null;

        // Ưu tiên tìm theo thứ tự: phone -> idNumber -> insuranceNumber
        if (phone != null && !phone.trim().isEmpty()) {
            records = medicalRecordRepository.findByPatientPhone(phone.trim());
            if (!records.isEmpty() && records.get(0).getPatient() != null) {
                patient = records.get(0).getPatient();
            } else {
                // Tìm patient trực tiếp nếu không có record
                patient = patientRepository.findByPhone(phone.trim()).orElse(null);
            }
        } else if (idNumber != null && !idNumber.trim().isEmpty()) {
            records = medicalRecordRepository.findByPatientIdNumber(idNumber.trim());
            if (!records.isEmpty() && records.get(0).getPatient() != null) {
                patient = records.get(0).getPatient();
            } else {
                patient = patientRepository.findByIdNumber(idNumber.trim()).orElse(null);
            }
        } else if (insuranceNumber != null && !insuranceNumber.trim().isEmpty()) {
            records = medicalRecordRepository.findByPatientInsuranceNumber(insuranceNumber.trim());
            if (!records.isEmpty() && records.get(0).getPatient() != null) {
                patient = records.get(0).getPatient();
            } else {
                patient = patientRepository.findByInsuranceNumber(insuranceNumber.trim()).orElse(null);
            }
        }

        // Nếu không tìm thấy bệnh nhân
        if (patient == null) {
            return PatientHistorySearchResponse.builder()
                    .medicalRecords(List.of())
                    .totalRecords(0)
                    .build();
        }

        // Nếu tìm thấy patient nhưng chưa có records, lấy records theo patientId
        if (records == null || records.isEmpty()) {
            records = medicalRecordRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patient.getPatientId());
        }

        List<MedicalRecordResponse> recordResponses = records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PatientHistorySearchResponse.builder()
                .patientId(patient.getPatientId())
                .patientName(patient.getFullName())
                .phone(patient.getPhone())
                .idNumber(patient.getIdNumber())
                .insuranceNumber(patient.getInsuranceNumber())
                .email(patient.getEmail())
                .dob(patient.getDob())
                .gender(patient.getGender() != null ? patient.getGender().getLabel() : null)
                .address(patient.getAddress())
                .medicalRecords(recordResponses)
                .totalRecords(recordResponses.size())
                .build();
    }
}