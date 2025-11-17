package com.example.demo.service;

import com.example.demo.dto.AddPrescriptionRequest;
import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.dto.MedicalRecordDetailResponse;
import com.example.demo.dto.PrescriptionResponse;
import com.example.demo.dto.UpdateMedicalRecordRequest;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Prescription;
import com.example.demo.entity.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.PrescriptionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PrescriptionRepository prescriptionRepository,
                                UserRepository userRepository,
                                JwtUtil jwtUtil) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
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
        String patientName = null;

        if (request.getPatientId() != null) {
            patient = userRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            patientName = patient.getFullName();
        } else if (request.getPatientName() != null && !request.getPatientName().trim().isEmpty()) {
            patientName = request.getPatientName().trim();
        }

        MedicalRecord record = MedicalRecord.builder()
                .doctor(doctor)
                .patient(patient)
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + request.getRecordId()));

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
        return medicalRecordRepository.findByPatient_UserIdOrderByCreatedAtDesc(patientId)
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

    @Transactional
    public MedicalRecordDetailResponse updateRecord(UUID recordId, UpdateMedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh án với ID: " + recordId));
        if (request.getPatientId() != null) {
            User patient = userRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + request.getPatientId()));
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
                        .instructions(prescriptionRequest.getInstructions() != null ? prescriptionRequest.getInstructions() : "")
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
                .patientId(record.getPatient() != null ? record.getPatient().getUserId() : null)
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
                .patientId(r.getPatient() != null ? r.getPatient().getUserId() : null)
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
}