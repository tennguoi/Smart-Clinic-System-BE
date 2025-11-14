package com.example.demo.repository;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    
    // Find by patient
    List<MedicalRecord> findByPatientOrderByExaminationDateDesc(Patient patient);
    
    Page<MedicalRecord> findByPatientOrderByExaminationDateDesc(Patient patient, Pageable pageable);
    
    // Find by doctor
    List<MedicalRecord> findByDoctorOrderByExaminationDateDesc(User doctor);
    
    Page<MedicalRecord> findByDoctorOrderByExaminationDateDesc(User doctor, Pageable pageable);
    
    // Find by patient and doctor
    List<MedicalRecord> findByPatientAndDoctorOrderByExaminationDateDesc(Patient patient, User doctor);
    
    // Find by status
    List<MedicalRecord> findByStatusOrderByExaminationDateDesc(MedicalRecord.Status status);
    
    Page<MedicalRecord> findByStatusOrderByExaminationDateDesc(MedicalRecord.Status status, Pageable pageable);
    
    // Find by date range
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.examinationDate BETWEEN :startDate AND :endDate ORDER BY mr.examinationDate DESC")
    List<MedicalRecord> findByExaminationDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find by patient and date range
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient = :patient AND mr.examinationDate BETWEEN :startDate AND :endDate ORDER BY mr.examinationDate DESC")
    List<MedicalRecord> findByPatientAndExaminationDateBetween(@Param("patient") Patient patient, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find by doctor and date range
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctor = :doctor AND mr.examinationDate BETWEEN :startDate AND :endDate ORDER BY mr.examinationDate DESC")
    List<MedicalRecord> findByDoctorAndExaminationDateBetween(@Param("doctor") User doctor, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count by patient
    long countByPatient(Patient patient);
    
    // Count by doctor
    long countByDoctor(User doctor);
    
    // Search in diagnosis and chief complaint
    @Query("SELECT mr FROM MedicalRecord mr WHERE " +
           "(LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(mr.chiefComplaint) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY mr.examinationDate DESC")
    Page<MedicalRecord> searchMedicalRecords(@Param("search") String search, Pageable pageable);
}
