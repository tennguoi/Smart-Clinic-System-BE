package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

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
}

