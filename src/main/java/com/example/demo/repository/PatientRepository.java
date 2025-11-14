package com.example.demo.repository;

import com.example.demo.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    Optional<Patient> findByPatientCode(String patientCode);
    
    Optional<Patient> findByPhone(String phone);
    
    Optional<Patient> findByEmail(String email);
    
    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
    
    Page<Patient> findByIsActiveTrue(Pageable pageable);
    
    Page<Patient> findByIsActiveTrueAndFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(p.fullName LIKE %:search% OR p.phone LIKE %:search% OR p.email LIKE %:search% OR p.patientCode LIKE %:search%)")
    Page<Patient> searchActivePatients(@Param("search") String search, Pageable pageable);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPatientCode(String patientCode);
    
    boolean existsByIdentityCard(String identityCard);
}
