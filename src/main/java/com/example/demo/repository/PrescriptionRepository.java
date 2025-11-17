package com.example.demo.repository; // (Package repository của bạn)

import com.example.demo.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    // (Bạn có thể thêm các phương thức tìm kiếm tùy chỉnh ở đây sau)
}