package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
        String fullName,
        
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
        String phone,
        
        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        String email,
        
        LocalDate dateOfBirth,
        
        String gender, // MALE, FEMALE, OTHER
        
        @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
        String address,
        
        @Size(max = 20, message = "CMND/CCCD không được vượt quá 20 ký tự")
        String identityCard,
        
        @Size(max = 20, message = "Số bảo hiểm không được vượt quá 20 ký tự")
        String insuranceNumber,
        
        @Size(max = 100, message = "Tên người liên hệ khẩn cấp không được vượt quá 100 ký tự")
        String emergencyContact,
        
        @Pattern(regexp = "^(\\+84|0)[0-9]{9}$|^$", message = "Số điện thoại khẩn cấp không hợp lệ")
        String emergencyPhone,
        
        String medicalHistory,
        
        String allergies,
        
        String notes
) {}
