package com.example.demo.service;

import com.example.demo.dto.ServiceAssignmentRequest;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Patient;
import com.example.demo.entity.ServiceAssignment;
import com.example.demo.entity.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.ServiceAssignmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceAssignmentService {

    private final ServiceAssignmentRepository serviceAssignmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceRepository serviceRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public ServiceAssignment createServiceAssignment(ServiceAssignmentRequest request, String token) {
        // Get medical record
        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.medicalRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));

        // Get service - Service ID is String
        com.example.demo.entity.Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        // Get patient
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Get doctor from token
        String email = jwtUtil.getEmailFromToken(token);
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // Calculate total price
        BigDecimal unitPrice = request.unitPrice() != null ? request.unitPrice() : 
                (service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO);
        Integer quantity = request.quantity() != null ? request.quantity() : 1;
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Create service assignment
        ServiceAssignment assignment = ServiceAssignment.builder()
                .medicalRecord(medicalRecord)
                .service(service)
                .patient(patient)
                .assignedBy(doctor)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .status(ServiceAssignment.Status.Pending)
                .notes(request.notes())
                .build();

        return serviceAssignmentRepository.save(assignment);
    }

    public List<ServiceAssignment> getServiceAssignmentsByMedicalRecord(UUID medicalRecordId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));
        
        return serviceAssignmentRepository.findByMedicalRecord(medicalRecord);
    }

    public List<ServiceAssignment> getServiceAssignmentsByPatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        return serviceAssignmentRepository.findByPatient(patient);
    }
}

