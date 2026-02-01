// src/main/java/com/example/demo/repository/BillingRecordRepository.java
package com.example.demo.repository;

import com.example.demo.entity.BillingRecord;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, UUID> {

    // ==================== TÌM KIẾM CHUNG ====================
    @Query("""
        SELECT b FROM BillingRecord b
        JOIN b.medicalRecord mr
        JOIN mr.patient p
        WHERE :search IS NULL OR :search = ''
           OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<BillingRecord> search(@Param("search") String search, Pageable pageable);

    // ==================== LẤY HÓA ĐƠN THEO TRẠNG THÁI ====================
    Page<BillingRecord> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    // ==================== TÌM HÓA ĐƠN PENDING (CHỜ THANH TOÁN) ====================
    @Query("""
        SELECT b FROM BillingRecord b
        JOIN b.medicalRecord mr
        JOIN mr.patient p
        WHERE b.paymentStatus = 'Pending'
        AND (:search IS NULL OR :search = ''
             OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<BillingRecord> searchPending(@Param("search") String search, Pageable pageable);

    // ==================== TÌM HÓA ĐƠN THEO MEDICAL RECORD ====================
    Optional<BillingRecord> findByMedicalRecord_RecordId(UUID recordId);

    // ==================== DOANH THU ====================
    // Tính tổng doanh thu từ các hóa đơn đã thanh toán
    @Query("SELECT SUM(b.amountPaid) FROM BillingRecord b WHERE b.paymentStatus = 'Paid'")
    BigDecimal sumTotalRevenue();

    // Tính tổng doanh thu trong khoảng thời gian (chỉ tính hóa đơn đã thanh toán)
    @Query("SELECT SUM(b.amountPaid) FROM BillingRecord b WHERE b.paymentStatus = 'Paid' AND b.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Lấy xu hướng doanh thu theo ngày
    @Query("""
        SELECT new com.example.demo.dto.TrendDataPoint(
            FUNCTION('DATE_FORMAT', b.createdAt, '%d/%m'), 
            SUM(b.amountPaid), 
            ''
        )
        FROM BillingRecord b
        WHERE b.paymentStatus = 'Paid' AND b.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', b.createdAt, '%d/%m')
        ORDER BY FUNCTION('DATE_FORMAT', b.createdAt, '%d/%m')
    """)
    List<com.example.demo.dto.TrendDataPoint> findRevenueTrendBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Đếm số lượng hóa đơn trong khoảng thời gian
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<BillingRecord> findByUsers_UserId(UUID doctorId, Pageable pageable);

    // Tìm kiếm hóa đơn theo bác sĩ + từ khóa
    @Query("SELECT b FROM BillingRecord b " +
            "WHERE b.users.userId = :doctorId " +
            "AND (LOWER(b.medicalRecord.patient.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(b.medicalRecord.patient.phone) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR CAST(b.billId AS string) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<BillingRecord> searchByDoctorId(@Param("doctorId") UUID doctorId,
                                         @Param("search") String search,
                                         Pageable pageable);
}