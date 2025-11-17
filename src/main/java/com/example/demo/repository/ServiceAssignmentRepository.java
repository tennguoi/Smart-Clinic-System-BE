package com.example.demo.repository;

import com.example.demo.entity.ServiceAssignment;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceAssignmentRepository extends JpaRepository<ServiceAssignment, UUID> {
    
    List<ServiceAssignment> findByMedicalRecord(MedicalRecord medicalRecord);
    
    List<ServiceAssignment> findByPatient(Patient patient);
    
    @Query("SELECT sa FROM ServiceAssignment sa WHERE sa.medicalRecord.recordId = :medicalRecordId")
    List<ServiceAssignment> findByMedicalRecordId(@Param("medicalRecordId") UUID medicalRecordId);
    
    @Query("SELECT sa FROM ServiceAssignment sa WHERE sa.patient.patientId = :patientId AND sa.status = 'Completed'")
    List<ServiceAssignment> findCompletedByPatient(@Param("patientId") UUID patientId);
}

