// src/main/java/com/example/demo/repository/InvoiceRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Thêm method tìm theo tên bệnh nhân (dùng trong search)
    @Query("SELECT i FROM Invoice i WHERE LOWER(i.patient.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Invoice> findAllByPatientFullNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}