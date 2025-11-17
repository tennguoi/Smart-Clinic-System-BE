package com.example.demo.service;

import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                UserRepository userRepository,
                                JwtUtil jwtUtil) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

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