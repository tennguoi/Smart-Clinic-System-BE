package com.example.demo.dto.appointment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentRequest(
        @NotBlank(message = "Họ và tên bệnh nhân không được để trống")
        @Size(min = 2, max = 100, message = "Họ tên phải có độ dài từ 2 đến 100 ký tự")
        String patientName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Số điện thoại không đúng định dạng")
        String phone,

        @Email(message = "Địa chỉ email không đúng định dạng")
        String email,

        @NotNull(message = "Thời gian khám không được để trống")
        @Future(message = "Thời gian khám phải lớn hơn thời điểm hiện tại")
        LocalDateTime appointmentTime,

        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String notes,

        Boolean quickBook,

        List<String> serviceIds
) {}
