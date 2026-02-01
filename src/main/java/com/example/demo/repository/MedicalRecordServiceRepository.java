// src/main/java/com/example/demo/repository/MedicalRecordServiceRepository.java
package com.example.demo.repository;

import com.example.demo.entity.MedicalRecordServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordServiceRepository extends JpaRepository<MedicalRecordServiceEntity, UUID> {
    List<MedicalRecordServiceEntity> findAllByMedicalRecord_RecordId(UUID recordId);

    // Lấy tất cả các dịch vụ từ hồ sơ bệnh án hoàn thành (có services)
    @Query("SELECT mrs FROM MedicalRecordServiceEntity mrs " +
            "LEFT JOIN FETCH mrs.medicalRecord mr " +
            "LEFT JOIN FETCH mrs.service s " +
            "WHERE mr.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY mr.createdAt DESC")
    List<MedicalRecordServiceEntity> findCompletedRecordsWithServices(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}