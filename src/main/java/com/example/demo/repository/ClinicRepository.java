package com.example.demo.repository;

import com.example.demo.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, UUID> {
    // Lấy thông tin phòng khám (chỉ có 1 bản ghi)
    Optional<Clinic> findFirstByOrderByCreatedAtAsc();
}

