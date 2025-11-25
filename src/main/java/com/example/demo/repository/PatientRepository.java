package com.example.demo.repository;

import com.example.demo.entity.Patient;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByPhone(String phone);
    Optional<Patient> findByEmail(String email);
    Page<Patient> findByFullNameContainingIgnoreCaseOrPhoneContaining(
            String fullName,
            String phone,
            Pageable pageable
    );

    /**
     * Tìm kiếm nâng cao hơn (nếu cần)
     */
    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.phone LIKE CONCAT('%', :search, '%')")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);


    /**
     * Tìm bệnh nhân theo CMND/CCCD (để check trùng)
     */
    Optional<Patient> findByIdNumber(String idNumber);

    /**
     * Tìm bệnh nhân theo số bảo hiểm (để check trùng)
     */
    Optional<Patient> findByInsuranceNumber(String insuranceNumber);
}