package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    boolean existsByInvoiceCode(String code);

    // Thêm method tìm theo tên bệnh nhân (dùng trong search)
    @Query("SELECT i FROM Invoice i WHERE LOWER(i.patient.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Invoice> findAllByPatientFullNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i")
    BigDecimal sumTotalRevenue();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new com.example.demo.dto.TrendDataPoint(FUNCTION('DATE_FORMAT', i.createdAt, '%d/%m'), SUM(i.totalAmount), '') " +
            "FROM Invoice i " +
            "WHERE i.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE_FORMAT', i.createdAt, '%d/%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', i.createdAt, '%d/%m')")
    java.util.List<com.example.demo.dto.TrendDataPoint> findRevenueTrendBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}