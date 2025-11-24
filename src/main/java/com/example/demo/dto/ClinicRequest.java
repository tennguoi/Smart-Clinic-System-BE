package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClinicRequest {
    @NotBlank(message = "Tên phòng khám không được để trống")
    private String name;
    
    private String address;
    
    private String phone;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    private String website;
    
    private String logoUrl;
}

