package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_patient", columnList = "patient_id"),
        @Index(name = "idx_payment_queue", columnList = "queue_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_code", columnList = "payment_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @PrePersist
    public void prePersist() {
        if (paymentId == null) paymentId = UUID.randomUUID();
        if (paymentDate == null) paymentDate = LocalDateTime.now();
        if (status == null) status = Status.Pending;
        if (paymentCode == null) paymentCode = generatePaymentCode();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id")
    private Queue queue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private User processedBy; // Lễ tân xử lý thanh toán

    @Column(name = "payment_code", nullable = false, unique = true, length = 20)
    private String paymentCode;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    private String generatePaymentCode() {
        return "PAY" + System.currentTimeMillis() % 1000000;
    }

    public enum PaymentMethod {
        Cash,           // Tiền mặt
        Card,           // Thẻ
        BankTransfer    // Chuyển khoản
    }

    public enum Status {
        Pending,    // Chờ thanh toán
        Completed,  // Đã thanh toán
        Cancelled,  // Đã hủy
        Refunded    // Đã hoàn tiền
    }
}

