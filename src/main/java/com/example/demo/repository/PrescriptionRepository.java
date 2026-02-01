package com.example.demo.repository;

import com.example.demo.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    /**
     * Tìm đơn thuốc theo recordId (One-to-One relationship)
     */
    @Query("SELECT p FROM Prescription p WHERE p.medicalRecord.recordId = :recordId")
    Optional<Prescription> findByRecordId(UUID recordId);
}