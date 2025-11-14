package com.example.demo.repository;

import com.example.demo.entity.Patient;
import com.example.demo.entity.Prescription;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    
    // Find by prescription code
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    
    // Check if prescription code exists
    boolean existsByPrescriptionCode(String prescriptionCode);
    
    // Find by patient
    List<Prescription> findByPatientOrderByPrescribedDateDesc(Patient patient);
    
    Page<Prescription> findByPatientOrderByPrescribedDateDesc(Patient patient, Pageable pageable);
    
    // Find by doctor
    List<Prescription> findByDoctorOrderByPrescribedDateDesc(User doctor);
    
    Page<Prescription> findByDoctorOrderByPrescribedDateDesc(User doctor, Pageable pageable);
    
    // Find by patient and doctor
    List<Prescription> findByPatientAndDoctorOrderByPrescribedDateDesc(Patient patient, User doctor);
    
    // Find by status
    List<Prescription> findByStatusOrderByPrescribedDateDesc(Prescription.Status status);
    
    Page<Prescription> findByStatusOrderByPrescribedDateDesc(Prescription.Status status, Pageable pageable);
    
    // Find by date range
    @Query("SELECT p FROM Prescription p WHERE p.prescribedDate BETWEEN :startDate AND :endDate ORDER BY p.prescribedDate DESC")
    List<Prescription> findByPrescribedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find by patient and date range
    @Query("SELECT p FROM Prescription p WHERE p.patient = :patient AND p.prescribedDate BETWEEN :startDate AND :endDate ORDER BY p.prescribedDate DESC")
    List<Prescription> findByPatientAndPrescribedDateBetween(@Param("patient") Patient patient, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find by doctor and date range
    @Query("SELECT p FROM Prescription p WHERE p.doctor = :doctor AND p.prescribedDate BETWEEN :startDate AND :endDate ORDER BY p.prescribedDate DESC")
    List<Prescription> findByDoctorAndPrescribedDateBetween(@Param("doctor") User doctor, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count by patient
    long countByPatient(Patient patient);
    
    // Count by doctor
    long countByDoctor(User doctor);
    
    // Search in diagnosis and medications
    @Query("SELECT p FROM Prescription p JOIN p.items i WHERE " +
           "(LOWER(p.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.medicationName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.prescribedDate DESC")
    Page<Prescription> searchPrescriptions(@Param("search") String search, Pageable pageable);
    
    // Find active prescriptions for a patient
    @Query("SELECT p FROM Prescription p WHERE p.patient = :patient AND p.status = 'ACTIVE' ORDER BY p.prescribedDate DESC")
    List<Prescription> findActivePrescriptionsByPatient(@Param("patient") Patient patient);
}
