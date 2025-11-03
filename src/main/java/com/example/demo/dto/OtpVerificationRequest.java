package com.example.demo.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otpCode;
}