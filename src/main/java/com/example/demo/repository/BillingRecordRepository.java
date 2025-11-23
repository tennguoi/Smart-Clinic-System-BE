// src/main/java/com/example/demo/repository/BillingRecordRepository.java
package com.example.demo.repository;

import com.example.demo.entity.BillingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, UUID> {

    @Query("""
        SELECT b FROM BillingRecord b
        JOIN b.medicalRecord mr
        JOIN mr.patient p
        WHERE :search IS NULL OR :search = ''
           OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<BillingRecord> search(@Param("search") String search, Pageable pageable);
}