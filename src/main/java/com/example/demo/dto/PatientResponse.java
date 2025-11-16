package com.example.demo.dto;

import com.example.demo.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private UUID patientId;
    private String fullName;
    private String phone;
    private String email;
    private LocalDate dob;
    private String gender;
    private String address;
    private String idNumber;
    private String insuranceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor từ Entity
    public PatientResponse(Patient patient) {
        this.patientId = patient.getPatientId();
        this.fullName = patient.getFullName();
        this.phone = patient.getPhone();
        this.email = patient.getEmail();
        this.dob = patient.getDob();
        this.gender = patient.getGender() != null ? patient.getGender().name() : null;
        this.address = patient.getAddress();
        this.idNumber = patient.getIdNumber();
        this.insuranceNumber = patient.getInsuranceNumber();
        this.notes = patient.getNotes();
        this.createdAt = patient.getCreatedAt();
        this.updatedAt = patient.getUpdatedAt();
    }
}