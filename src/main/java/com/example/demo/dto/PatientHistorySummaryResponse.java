package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO để hiển thị tóm tắt lịch sử khám của bệnh nhân
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHistorySummaryResponse {

    private UUID patientId; // null nếu là bệnh nhân vãng lai
    private String patientName; // Tên bệnh nhân
    private String phone; // Số điện thoại
    private String idNumber; // CCCD
    private String insuranceNumber; // BHYT
    private String email; // Email
    private int visitCount; // Số lần khám
    private LocalDateTime lastVisitDate; // Ngày khám gần nhất

    // Danh sách các lần khám (optional - khi xem chi tiết)
    private List<MedicalRecordResponse> records;
}
