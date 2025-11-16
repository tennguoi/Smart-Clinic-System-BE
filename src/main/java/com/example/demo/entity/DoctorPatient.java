// com/example/demo/entity/DoctorPatient.java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_patient")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@IdClass(DoctorPatientId.class)
public class DoctorPatient {

    @Id
    @Column(name = "doctor_id", length = 36)
    private String doctorId;

    @Id
    @Column(name = "patient_id", length = 36)
    private String patientId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    // Quan hệ với Patient
    @ManyToOne
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;
}