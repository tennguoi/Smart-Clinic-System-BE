// src/main/java/com/example/demo/repository/MedicalRecordServiceRepository.java
package com.example.demo.repository;

import com.example.demo.entity.MedicalRecordServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordServiceRepository extends JpaRepository<MedicalRecordServiceEntity, UUID> {
    List<MedicalRecordServiceEntity> findAllByMedicalRecord_RecordId(UUID recordId);
}