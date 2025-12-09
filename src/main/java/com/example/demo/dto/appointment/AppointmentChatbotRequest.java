package com.example.demo.dto.appointment;

import jakarta.validation.constraints.NotBlank;
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
    private String patientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;

    private String dob;

    private String idNumber;

    @NotBlank(message = "Thời gian khám không được để trống")
    private String appointmentTime;

    private String notes;
}