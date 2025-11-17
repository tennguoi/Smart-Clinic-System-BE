package com.example.demo.service;

import com.example.demo.dto.MedicalRecordRequest;
import com.example.demo.dto.MedicalRecordResponse;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public Page<MedicalRecordResponse> getAllMedicalRecords(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return medicalRecordRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<MedicalRecordResponse> getMedicalRecordsByPatient(UUID patientId, int page, int size, String sortBy, String sortDir) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return medicalRecordRepository.findByPatientOrderByExaminationDateDesc(patient, pageable)
                .map(this::toResponse);
    }

    public Page<MedicalRecordResponse> getMedicalRecordsByDoctor(String token, int page, int size, String sortBy, String sortDir) {
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return medicalRecordRepository.findByDoctorOrderByExaminationDateDesc(doctor, pageable)
                .map(this::toResponse);
    }

    public Optional<MedicalRecordResponse> getMedicalRecordById(UUID id) {
        return medicalRecordRepository.findById(id)
                .map(this::toResponse);
    }

    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request, String token) {
        // Get doctor from token
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // Get patient
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Create medical record
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .examinationDate(request.examinationDate())
                .chiefComplaint(request.chiefComplaint())
                .historyOfPresentIllness(request.historyOfPresentIllness())
                .physicalExamination(request.physicalExamination())
                .vitalSigns(request.vitalSigns())
                .diagnosis(request.diagnosis())
                .differentialDiagnosis(request.differentialDiagnosis())
                .treatmentPlan(request.treatmentPlan())
                .medications(request.medications())
                .followUpInstructions(request.followUpInstructions())
                .additionalNotes(request.additionalNotes())
                .status(request.status() != null 
                    ? MedicalRecord.Status.valueOf(request.status().toUpperCase()) 
                    : MedicalRecord.Status.DRAFT)
                .build();

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);
        return toResponse(saved);
    }

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(UUID id, MedicalRecordRequest request, String token) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));

        // Verify doctor authorization (only the doctor who created it can update)
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!medicalRecord.getDoctor().getUserId().equals(doctor.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to update this medical record");
        }

        // Update fields
        medicalRecord.setExaminationDate(request.examinationDate());
        medicalRecord.setChiefComplaint(request.chiefComplaint());
        medicalRecord.setHistoryOfPresentIllness(request.historyOfPresentIllness());
        medicalRecord.setPhysicalExamination(request.physicalExamination());
        medicalRecord.setVitalSigns(request.vitalSigns());
        medicalRecord.setDiagnosis(request.diagnosis());
        medicalRecord.setDifferentialDiagnosis(request.differentialDiagnosis());
        medicalRecord.setTreatmentPlan(request.treatmentPlan());
        medicalRecord.setMedications(request.medications());
        medicalRecord.setFollowUpInstructions(request.followUpInstructions());
        medicalRecord.setAdditionalNotes(request.additionalNotes());
        
        if (request.status() != null) {
            medicalRecord.setStatus(MedicalRecord.Status.valueOf(request.status().toUpperCase()));
        }

        MedicalRecord updated = medicalRecordRepository.save(medicalRecord);
        return toResponse(updated);
    }

    @Transactional
    public void deleteMedicalRecord(UUID id, String token) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));

        // Verify doctor authorization
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!medicalRecord.getDoctor().getUserId().equals(doctor.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to delete this medical record");
        }

        medicalRecordRepository.delete(medicalRecord);
    }

    public List<MedicalRecordResponse> getPatientHistory(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        return medicalRecordRepository.findByPatientOrderByExaminationDateDesc(patient)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportMedicalRecordToPDF(UUID recordId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));
        
        try {
            return com.example.demo.util.PdfGenerator.generateMedicalRecordPDF(medicalRecord);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    public byte[] exportPatientHistoryToPDF(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientOrderByExaminationDateDesc(patient);
        
        try {
            // For now, generate PDF for the most recent record
            // Could be enhanced to generate a combined PDF with all records
            if (!records.isEmpty()) {
                return com.example.demo.util.PdfGenerator.generateMedicalRecordPDF(records.get(0));
            } else {
                throw new IllegalArgumentException("No medical records found for patient");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private MedicalRecordResponse toResponse(MedicalRecord medicalRecord) {
        return new MedicalRecordResponse(
                medicalRecord.getRecordId(),
                medicalRecord.getPatient().getPatientId(),
                medicalRecord.getPatient().getFullName(),
                medicalRecord.getPatient().getPatientCode(),
                medicalRecord.getDoctor().getUserId(),
                medicalRecord.getDoctor().getFullName(),
                medicalRecord.getExaminationDate(),
                medicalRecord.getChiefComplaint(),
                medicalRecord.getHistoryOfPresentIllness(),
                medicalRecord.getPhysicalExamination(),
                medicalRecord.getVitalSigns(),
                medicalRecord.getDiagnosis(),
                medicalRecord.getDifferentialDiagnosis(),
                medicalRecord.getTreatmentPlan(),
                medicalRecord.getMedications(),
                medicalRecord.getFollowUpInstructions(),
                medicalRecord.getAdditionalNotes(),
                medicalRecord.getStatus().name(),
                medicalRecord.getCreatedAt(),
                medicalRecord.getUpdatedAt()
        );
    }
}
