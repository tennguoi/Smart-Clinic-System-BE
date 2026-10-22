package com.example.demo.dto.appointment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentChatbotRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String patientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String dob;

    private String idNumber;

    @NotBlank(message = "Thời gian khám không được để trống")
    private String appointmentTime;

    @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
    private String notes;
}