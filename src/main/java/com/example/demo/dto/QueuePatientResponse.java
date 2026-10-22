package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePatientResponse {

    // Queue information
    private UUID queueId;
    private String queueNumber; // A001, A002...
    private String priority; // "Bình thường", "Ưu tiên", "Khẩn cấp"
    private String status; // "Đang chờ", "Đang khám", "Hoàn tất", "Hủy"
    private String checkInTime; // Thời gian check-in (ISO format)

    // Patient information
    private UUID patientId;

    @jakarta.validation.constraints.Size(max = 100, message = "Tên bệnh nhân không vượt quá 100 ký tự")
    private String patientName;

    @jakarta.validation.constraints.Pattern(regexp = "^(0|\\+84)?\\d{9,10}$|^$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @jakarta.validation.constraints.Email(message = "Email không đúng định dạng")
    private String email;

    private LocalDate dob;
    private String gender; // "Nam", "Nữ", "Khác"
    private String address;

    @jakarta.validation.constraints.Pattern(regexp = "^\\d{9}|\\d{12}$|^$", message = "Số CCCD/CMND phải gồm 9 hoặc 12 chữ số")
    private String idNumber; // CCCD/CMND

    private String insuranceNumber; // Số bảo hiểm y tế
    private String notes; // Ghi chú

    // Room information (nếu đã được phân phòng)
    private UUID roomId;
    private String roomName; // Tên phòng khám
    private String doctorName;
    // ✨ Appointment info - MỚI
    private boolean hasAppointment; // Đánh dấu có đặt lịch hay không
    private String appointmentCode; // Tên bác sĩ phụ trách phòng

    // ✨ Duplicate patient detection - MỚI
    private boolean existingPatientFound; // Báo hiệu có tìm thấy bệnh nhân đã tồn tại
    private String duplicateField; // Trường bị trùng: phone, idNumber, insuranceNumber

    // ✨ Services - MỚI
    private java.util.List<ExaminationSummaryResponse.ExaminationServiceItem> services;
}