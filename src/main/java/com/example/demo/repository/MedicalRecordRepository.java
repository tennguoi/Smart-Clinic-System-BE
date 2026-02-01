package com.example.demo.repository;

import com.example.demo.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository
                extends JpaRepository<MedicalRecord, UUID>, JpaSpecificationExecutor<MedicalRecord> {
        // ĐÚNG – DÙNG patientId
        List<MedicalRecord> findByPatient_PatientIdOrderByCreatedAtDesc(UUID patientId);

        // ĐÚNG – DÙNG doctor.userId
        List<MedicalRecord> findByDoctor_UserIdOrderByCreatedAtDesc(UUID doctorId);

        // Thêm vào file MedicalRecordRepository.java
        List<MedicalRecord> findByDoctor_UserIdAndPatientNameAndCreatedAtAfter(
                        UUID doctorId, String patientName, LocalDateTime createdAt);

        // DÙNG @Query ĐÚNG 100% – tránh lỗi tên method
        @Query("SELECT mr FROM MedicalRecord mr " +
                        "LEFT JOIN FETCH mr.doctor " +
                        "LEFT JOIN FETCH mr.patient " +
                        "WHERE mr.patient.patientId = :patientId " +
                        "ORDER BY mr.createdAt DESC")
        List<MedicalRecord> findByPatientIdWithFetch(@Param("patientId") UUID patientId);

        // DÙNG @Query ĐÚNG CHO DOCTOR
        @Query("SELECT mr FROM MedicalRecord mr " +
                        "LEFT JOIN FETCH mr.doctor " +
                        "LEFT JOIN FETCH mr.patient " +
                        "WHERE mr.doctor.userId = :doctorId " +
                        "ORDER BY mr.createdAt DESC")
        List<MedicalRecord> findByDoctorIdWithFetch(@Param("doctorId") UUID doctorId);

        // Các method khác nếu cần
        @Query("SELECT mr FROM MedicalRecord mr " +
                        "WHERE mr.doctor.userId = :doctorId " +
                        "AND mr.patient.patientId = :patientId " +
                        "AND mr.createdAt >= :fromDate")
        List<MedicalRecord> findByDoctor_UserIdAndPatient_PatientIdAndCreatedAtAfter(
                        UUID doctorId, UUID patientId, LocalDateTime fromDate);

        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        Optional<MedicalRecord> findFirstByPatient_PatientIdOrderByCreatedAtDesc(UUID patientId);

        boolean existsByPatient_PatientId(UUID patientId);

        @Query("SELECT mr FROM MedicalRecord mr " +
                        "LEFT JOIN FETCH mr.patient p " +
                        "LEFT JOIN FETCH mr.doctor " +
                        "WHERE p.phone = :phone " +
                        "ORDER BY mr.createdAt DESC")
        List<MedicalRecord> findByPatientPhone(@Param("phone") String phone);

        @Query("SELECT mr FROM MedicalRecord mr " +
                        "LEFT JOIN FETCH mr.patient p " +
                        "LEFT JOIN FETCH mr.doctor " +
                        "WHERE p.idNumber = :idNumber " +
                        "ORDER BY mr.createdAt DESC")
        List<MedicalRecord> findByPatientIdNumber(@Param("idNumber") String idNumber);

        @Query("SELECT mr FROM MedicalRecord mr " +
                        "LEFT JOIN FETCH mr.patient p " +
                        "LEFT JOIN FETCH mr.doctor " +
                        "WHERE p.insuranceNumber = :insuranceNumber " +
                        "ORDER BY mr.createdAt DESC")
        List<MedicalRecord> findByPatientInsuranceNumber(@Param("insuranceNumber") String insuranceNumber);

        // ✅ NEW: Find by QueueID
        Optional<MedicalRecord> findByQueueId(UUID queueId);
}