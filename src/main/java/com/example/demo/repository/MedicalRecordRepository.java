package com.example.demo.repository;

import com.example.demo.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    // Thêm vào file MedicalRecordRepository.java
    List<MedicalRecord> findByDoctor_UserIdAndPatientNameAndCreatedAtAfter(
            UUID doctorId, String patientName, LocalDateTime createdAt);
    /**
     * Lấy danh sách hồ sơ bệnh án theo bác sĩ, sắp xếp theo thời gian tạo mới nhất trước
     * Sử dụng JOIN FETCH để tránh N+1 query problem
     */
    @Query("SELECT mr FROM MedicalRecord mr LEFT JOIN FETCH mr.doctor LEFT JOIN FETCH mr.patient WHERE mr.doctor.userId = :doctorId ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByDoctor_UserIdOrderByCreatedAtDesc(UUID doctorId);
    
    /**
     * Lấy danh sách hồ sơ bệnh án theo bệnh nhân, sắp xếp theo thời gian tạo mới nhất trước
     * Sử dụng JOIN FETCH để tránh N+1 query problem
     */
    @Query("SELECT mr FROM MedicalRecord mr LEFT JOIN FETCH mr.doctor LEFT JOIN FETCH mr.patient WHERE mr.patient.userId = :patientId ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByPatient_UserIdOrderByCreatedAtDesc(UUID patientId);
    
    // Giữ lại methods cũ để tương thích (nếu có code khác đang dùng)
    @Deprecated
    List<MedicalRecord> findByDoctor_UserId(UUID doctorId);
    
    @Deprecated
    List<MedicalRecord> findByPatient_UserId(UUID patientId);
}


