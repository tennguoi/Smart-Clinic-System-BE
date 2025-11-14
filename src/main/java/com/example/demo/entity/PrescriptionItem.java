package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "prescription_items", indexes = {
        @Index(name = "idx_prescription_item_prescription", columnList = "prescription_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {

    @Id
    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @PrePersist
    public void prePersist() {
        if (itemId == null) itemId = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName; // Tên thuốc

    @Column(name = "active_ingredient", length = 255)
    private String activeIngredient; // Hoạt chất

    @Column(name = "strength", length = 100)
    private String strength; // Nồng độ/hàm lượng (ví dụ: "500mg", "10ml")

    @Column(name = "dosage_form", length = 100)
    private String dosageForm; // Dạng bào chế (viên, chai, ống...)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Số lượng

    @Column(name = "unit", length = 50)
    private String unit; // Đơn vị (viên, chai, hộp...)

    @Column(name = "dosage_instructions", columnDefinition = "TEXT")
    private String dosageInstructions; // Cách dùng (ví dụ: "Uống 2 viên/lần, 3 lần/ngày sau ăn")

    @Column(name = "frequency", length = 100)
    private String frequency; // Tần suất (1 lần/ngày, 2 lần/ngày...)

    @Column(name = "duration", length = 100)
    private String duration; // Thời gian sử dụng (7 ngày, 2 tuần...)

    @Column(name = "administration_route", length = 100)
    private String administrationRoute; // Đường dùng (uống, bôi, nhỏ mắt...)

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions; // Hướng dẫn đặc biệt

    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions; // Lưu ý/Thận trọng

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice; // Giá đơn vị

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice; // Tổng giá

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Ghi chú

    // Helper method to calculate total price
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
