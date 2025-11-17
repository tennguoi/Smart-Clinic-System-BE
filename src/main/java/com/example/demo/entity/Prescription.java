package com.example.demo.entity; // (Gói package entity của bạn)

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions") // Tên bảng (thường là số nhiều, chữ thường)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    /**
     * Khóa chính (UUID)
     * Giống với @PrePersist, @GeneratedValue(strategy = GenerationType.UUID)
     * sẽ tự động tạo UUID khi bạn save() một entity mới.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "prescription_id", updatable = false, nullable = false, length = 36)
    private UUID prescriptionId;

    /**
     * Khóa ngoại, liên kết với MedicalRecord.
     * Đây là mối quan hệ Một-Một (One-to-One): Một bệnh án có một đơn thuốc.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false, unique = true)
    private MedicalRecord medicalRecord;

    /**
     * Tên thuốc kê đơn (NOT NULL)
     * dùng columnDefinition = "TEXT" để khớp với kiểu 'text' trong database.
     */
    @Column(name = "drugs", nullable = false, columnDefinition = "TEXT")
    private String drugs;

    /**
     * Hướng dẫn sử dụng (NOT NULL)
     */
    @Column(name = "instructions", nullable = false, columnDefinition = "TEXT")
    private String instructions;

    /**
     * Ngày giờ kê đơn
     * @CreationTimestamp sẽ tự động gán ngày giờ hiện tại khi tạo mới.
     */
    @CreationTimestamp
    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt;
}