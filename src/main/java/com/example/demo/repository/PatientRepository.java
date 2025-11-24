package com.example.demo.repository;

import com.example.demo.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    java.util.Optional<Patient> findByPhone(String phone);
}