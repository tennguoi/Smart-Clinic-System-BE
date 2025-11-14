package com.example.demo.entity;

import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// Cần đảm bảo Patient.java và User.java vẫn tồn tại trong package này
// Nếu không, bạn sẽ gặp lỗi 'Cannot resolve symbol'
@Entity
@Table(name = "medical_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue
    @Column(name = "record_id")
    private UUID recordId;

    // Quan hệ với Bệnh nhân
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Quan hệ với Bác sĩ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;

    // ===============================================
    // 🔥 CỘT LƯU TOA THUỐC (DẠNG JSON STRING)
    // ===============================================
    @Column(name = "prescriptions_json", columnDefinition = "TEXT")
    private String prescriptionsJson;

    // ===============================================
    // 🔥 CỘT LƯU DỊCH VỤ (DẠNG JSON STRING)
    // ===============================================
    @Column(name = "service_orders_json", columnDefinition = "TEXT")
    private String serviceOrdersJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}