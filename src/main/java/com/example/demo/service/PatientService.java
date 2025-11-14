package com.example.demo.service;

import com.example.demo.dto.PatientRequest;
import com.example.demo.dto.PatientResponse;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public Page<PatientResponse> getAllActivePatients(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return patientRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<PatientResponse> searchPatients(String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return patientRepository.searchActivePatients(search, pageable)
                .map(this::toResponse);
    }

    public Optional<PatientResponse> getPatientById(UUID id) {
        return patientRepository.findById(id)
                .filter(Patient::getIsActive)
                .map(this::toResponse);
    }

    public Optional<PatientResponse> getPatientByCode(String code) {
        return patientRepository.findByPatientCode(code)
                .filter(Patient::getIsActive)
                .map(this::toResponse);
    }

    public Optional<PatientResponse> getPatientByPhone(String phone) {
        return patientRepository.findByPhone(phone)
                .filter(Patient::getIsActive)
                .map(this::toResponse);
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request, String token) {
        // Validation
        validatePatientRequest(request, null);

        // Get current user
        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate unique patient code
        String patientCode = generateUniquePatientCode();

        Patient patient = Patient.builder()
                .patientCode(patientCode)
                .fullName(request.fullName())
                .phone(request.phone())
                .email(request.email())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender() != null ? Patient.Gender.valueOf(request.gender().toUpperCase()) : null)
                .address(request.address())
                .identityCard(request.identityCard())
                .insuranceNumber(request.insuranceNumber())
                .emergencyContact(request.emergencyContact())
                .emergencyPhone(request.emergencyPhone())
                .medicalHistory(request.medicalHistory())
                .allergies(request.allergies())
                .notes(request.notes())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        Patient saved = patientRepository.save(patient);
        return toResponse(saved);
    }

    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request, String token) {
        Patient patient = patientRepository.findById(id)
                .filter(Patient::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Validation (excluding current patient from uniqueness check)
        validatePatientRequest(request, id);

        // Update fields
        patient.setFullName(request.fullName());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender() != null ? Patient.Gender.valueOf(request.gender().toUpperCase()) : null);
        patient.setAddress(request.address());
        patient.setIdentityCard(request.identityCard());
        patient.setInsuranceNumber(request.insuranceNumber());
        patient.setEmergencyContact(request.emergencyContact());
        patient.setEmergencyPhone(request.emergencyPhone());
        patient.setMedicalHistory(request.medicalHistory());
        patient.setAllergies(request.allergies());
        patient.setNotes(request.notes());

        Patient updated = patientRepository.save(patient);
        return toResponse(updated);
    }

    @Transactional
    public void deletePatient(UUID id) {
        Patient patient = patientRepository.findById(id)
                .filter(Patient::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Soft delete
        patient.setIsActive(false);
        patientRepository.save(patient);
    }

    private void validatePatientRequest(PatientRequest request, UUID excludeId) {
        // Check phone uniqueness
        if (request.phone() != null && !request.phone().trim().isEmpty()) {
            Optional<Patient> existingByPhone = patientRepository.findByPhone(request.phone());
            if (existingByPhone.isPresent() && 
                existingByPhone.get().getIsActive() && 
                (excludeId == null || !existingByPhone.get().getPatientId().equals(excludeId))) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại");
            }
        }

        // Check email uniqueness if provided
        if (request.email() != null && !request.email().trim().isEmpty()) {
            Optional<Patient> existingByEmail = patientRepository.findByEmail(request.email());
            if (existingByEmail.isPresent() && 
                existingByEmail.get().getIsActive() && 
                (excludeId == null || !existingByEmail.get().getPatientId().equals(excludeId))) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
        }

        // Check identity card uniqueness if provided
        if (request.identityCard() != null && !request.identityCard().trim().isEmpty()) {
            boolean exists = patientRepository.existsByIdentityCard(request.identityCard());
            if (exists && excludeId == null) {
                // For update, we need to check if it belongs to a different patient
                throw new IllegalArgumentException("CMND/CCCD đã tồn tại");
            }
        }
    }

    private String generateUniquePatientCode() {
        String prefix = "BN";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        
        int attempts = 0;
        String code;
        do {
            int sequence = (int) (Math.random() * 9999) + 1;
            code = String.format("%s%s%04d", prefix, datePart, sequence);
            attempts++;
            if (attempts > 1000) {
                throw new IllegalStateException("Cannot generate unique patient code");
            }
        } while (patientRepository.existsByPatientCode(code));
        
        return code;
    }

    private PatientResponse toResponse(Patient patient) {
        String createdByName = null;
        if (patient.getCreatedBy() != null) {
            createdByName = patient.getCreatedBy().getFullName();
        }

        return new PatientResponse(
                patient.getPatientId(),
                patient.getPatientCode(),
                patient.getFullName(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getAddress(),
                patient.getIdentityCard(),
                patient.getInsuranceNumber(),
                patient.getEmergencyContact(),
                patient.getEmergencyPhone(),
                patient.getMedicalHistory(),
                patient.getAllergies(),
                patient.getNotes(),
                patient.getIsActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt(),
                createdByName
        );
    }
}
