package com.example.demo.dto.auth;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otpCode;
}