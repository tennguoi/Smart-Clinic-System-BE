package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ClinicRequest {
    // Không dùng @NotBlank để cho phép partial update
    // Validation sẽ được kiểm tra trong service khi tạo mới
    private String name;
    
    private String address;
    
    private String phone;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    private String website;
    
    private String logoUrl;
    
    private LocalTime morningStartTime;
    
    private LocalTime morningEndTime;
    
    private LocalTime afternoonStartTime;
    
    private LocalTime afternoonEndTime;
}

