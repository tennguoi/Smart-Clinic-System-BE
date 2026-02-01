package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MedicalRecordSearchCriteria {
    private String keyword;       // Tìm theo tên hoặc SĐT
    private LocalDate startDate;  // Ngày bắt đầu
    private LocalDate endDate;    // Ngày kết thúc
    private UUID doctorId;        // ID bác sĩ (Lấy từ token, không phải từ FE gửi lên)
}