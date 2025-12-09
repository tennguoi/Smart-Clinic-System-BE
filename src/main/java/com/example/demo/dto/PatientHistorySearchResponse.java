package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO trả về kết quả tìm kiếm lịch sử khám bệnh của bệnh nhân
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistorySearchResponse {

    // Thông tin bệnh nhân
    private UUID patientId;
    private String patientName;
    private String phone;
    private String idNumber;
    private String insuranceNumber;
    private String email;
    private LocalDate dob;
    private String gender;
    private String address;

    // Danh sách lịch sử khám
    private List<MedicalRecordResponse> medicalRecords;

    // Tổng số lần khám
    private int totalRecords;
}
