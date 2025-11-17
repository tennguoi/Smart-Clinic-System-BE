package com.example.demo.service;

import com.example.demo.dto.AddPrescriptionRequest; // <<< THÊM MỚI (DTO cho chức năng của bạn)
import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Prescription; // <<< THÊM MỚI (Entity của bạn)
import com.example.demo.entity.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.PrescriptionRepository; // <<< THÊM MỚI (Repository của bạn)
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Prescription;
import com.example.demo.repository.PrescriptionRepository;
import com.example.demo.dto.AddPrescriptionRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository; // <<< THÊM MỚI
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // --- CONSTRUCTOR (BẮT BUỘC PHẢI SỬA) ---
    // Chúng ta thêm PrescriptionRepository vào đây
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PrescriptionRepository prescriptionRepository, // <<< SỬA ĐỔI
                                UserRepository userRepository,
                                JwtUtil jwtUtil) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository; // <<< SỬA ĐỔI
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    //
    // --- PHẦN CODE CŨ CỦA ĐỒNG ĐỘI (GIỮ NGUYÊN 100%) ---
    //
    @Transactional
    public MedicalRecordResponse create(String bearerToken, MedicalRecordRequest request) {
        String token = extractToken(bearerToken);
        UUID doctorId = jwtUtil.getUserIdFromToken(token);
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        User patient = null;
        if (request.getPatientId() != null) {
            patient = userRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
        }

        MedicalRecord record = MedicalRecord.builder()
                .doctor(doctor)
                .patient(patient)
                .diagnosis(request.getDiagnosis())
                .treatmentNotes(request.getTreatmentNotes())
                .build();
        record = medicalRecordRepository.save(record);
        return toResponse(record);
    }

    //
    // --- PHẦN CODE MỚI CỦA BẠN (THÊM VÀO) ---
    // Chức năng: Thêm toa thuốc (drugs) và hướng dẫn (instructions)
    //
    @Transactional
    public void addPrescriptionToRecord(AddPrescriptionRequest request) {

        // 1. Tìm hồ sơ bệnh án cha (dùng repository cũ)
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("MedicalRecord not found with id: " + request.getRecordId()));

        // 2. Tạo đối tượng Prescription (Nhiệm vụ của bạn)
        Prescription prescription = Prescription.builder()
                .medicalRecord(record) // <<< Liên kết khóa ngoại
                .drugs(request.getDrugs())
                .instructions(request.getInstructions())
                .build();

        // 3. Lưu đơn thuốc (dùng repository mới)
        prescriptionRepository.save(prescription);
    }
    //
    // --- HẾT PHẦN CODE MỚI CỦA BẠN ---
    //


    //
    // --- PHẦN CODE CŨ CỦA ĐỒNG ĐỘI (GIỮ NGUYÊN 100%) ---
    //
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listByPatient(UUID patientId) {
        return medicalRecordRepository.findByPatient_UserId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listMyCreated(String bearerToken) {
        String token = extractToken(bearerToken);
        UUID doctorId = jwtUtil.getUserIdFromToken(token);
        return medicalRecordRepository.findByDoctor_UserId(doctorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .recordId(r.getRecordId())
                .patientId(r.getPatient() != null ? r.getPatient().getUserId() : null)
                .patientName(r.getPatient() != null ? r.getPatient().getFullName() : null)
                .doctorId(r.getDoctor() != null ? r.getDoctor().getUserId() : null)
                .doctorName(r.getDoctor() != null ? r.getDoctor().getFullName() : null)
                .diagnosis(r.getDiagnosis())
                .treatmentNotes(r.getTreatmentNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization token missing");
        }
        return bearerToken.substring(7);
    }
}