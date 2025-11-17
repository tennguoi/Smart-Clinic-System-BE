package com.example.demo.service;

import com.example.demo.dto.PrescriptionRequest;
import com.example.demo.dto.PrescriptionResponse;
import com.example.demo.dto.PrescriptionItemRequest;
import com.example.demo.dto.PrescriptionItemResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request, String token) {
        // Get doctor from token
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // Get patient
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Get medical record if provided
        MedicalRecord medicalRecord = null;
        if (request.medicalRecordId() != null) {
            medicalRecord = medicalRecordRepository.findById(request.medicalRecordId())
                    .orElse(null);
        }

        // Generate prescription code
        String prescriptionCode = generateUniquePrescriptionCode();

        // Create prescription
        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicalRecord(medicalRecord)
                .prescriptionCode(prescriptionCode)
                .prescribedDate(request.prescribedDate() != null ? request.prescribedDate() : LocalDateTime.now())
                .diagnosis(request.diagnosis())
                .treatmentInstructions(request.treatmentInstructions())
                .lifestyleAdvice(request.lifestyleAdvice())
                .followUpDate(request.followUpDate())
                .followUpInstructions(request.followUpInstructions())
                .additionalNotes(request.additionalNotes())
                .status(request.status() != null 
                    ? Prescription.Status.valueOf(request.status().toUpperCase()) 
                    : Prescription.Status.ACTIVE)
                .build();

        // Add prescription items
        if (request.items() != null && !request.items().isEmpty()) {
            for (PrescriptionItemRequest itemRequest : request.items()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicationName(itemRequest.medicationName())
                        .activeIngredient(itemRequest.activeIngredient())
                        .strength(itemRequest.strength())
                        .dosageForm(itemRequest.dosageForm())
                        .quantity(itemRequest.quantity())
                        .unit(itemRequest.unit())
                        .dosageInstructions(itemRequest.dosageInstructions())
                        .frequency(itemRequest.frequency())
                        .duration(itemRequest.duration())
                        .administrationRoute(itemRequest.administrationRoute())
                        .specialInstructions(itemRequest.specialInstructions())
                        .precautions(itemRequest.precautions())
                        .unitPrice(itemRequest.unitPrice())
                        .notes(itemRequest.notes())
                        .build();
                
                item.calculateTotalPrice();
                prescription.addItem(item);
            }
        }

        prescription = prescriptionRepository.save(prescription);
        return toResponse(prescription);
    }

    public PrescriptionResponse getPrescriptionById(UUID prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));
        return toResponse(prescription);
    }

    public PrescriptionResponse getPrescriptionByCode(String prescriptionCode) {
        Prescription prescription = prescriptionRepository.findByPrescriptionCode(prescriptionCode)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));
        return toResponse(prescription);
    }

    public List<PrescriptionResponse> getPrescriptionsByPatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        return prescriptionRepository.findByPatientOrderByPrescribedDateDesc(patient)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<PrescriptionResponse> getPrescriptionsByPatient(UUID patientId, int page, int size) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescribedDate"));
        return prescriptionRepository.findByPatientOrderByPrescribedDateDesc(patient, pageable)
                .map(this::toResponse);
    }

    public List<PrescriptionResponse> getPrescriptionsByDoctor(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        
        return prescriptionRepository.findByDoctorOrderByPrescribedDateDesc(doctor)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String generateUniquePrescriptionCode() {
        String code;
        int attempts = 0;
        do {
            code = "RX" + CodeGenerator.generate(8);
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Cannot generate unique prescription code");
        } while (prescriptionRepository.existsByPrescriptionCode(code));
        return code;
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        List<PrescriptionItemResponse> items = prescription.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return new PrescriptionResponse(
                prescription.getPrescriptionId(),
                prescription.getPrescriptionCode(),
                prescription.getPatient().getPatientId(),
                prescription.getPatient().getFullName(),
                prescription.getPatient().getPatientCode(),
                prescription.getDoctor().getUserId(),
                prescription.getDoctor().getFullName(),
                prescription.getMedicalRecord() != null ? prescription.getMedicalRecord().getRecordId() : null,
                prescription.getPrescribedDate(),
                prescription.getDiagnosis(),
                prescription.getTreatmentInstructions(),
                prescription.getLifestyleAdvice(),
                prescription.getFollowUpDate(),
                prescription.getFollowUpInstructions(),
                prescription.getAdditionalNotes(),
                prescription.getStatus().name(),
                items,
                prescription.getCreatedAt(),
                prescription.getUpdatedAt()
        );
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItem item) {
        return new PrescriptionItemResponse(
                item.getItemId(),
                item.getMedicationName(),
                item.getActiveIngredient(),
                item.getStrength(),
                item.getDosageForm(),
                item.getQuantity(),
                item.getUnit(),
                item.getDosageInstructions(),
                item.getFrequency(),
                item.getDuration(),
                item.getAdministrationRoute(),
                item.getSpecialInstructions(),
                item.getPrecautions(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getNotes()
        );
    }
}

