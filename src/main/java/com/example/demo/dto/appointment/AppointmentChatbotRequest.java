package com.example.demo.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Thời gian khám không được để trống")
    private String appointmentTime; // Format: "YYYY-MM-DD HH:mm"

    private String notes; // Optional
}
