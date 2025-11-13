package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String token;
    private List<String> roles;
    private String message;
    private Boolean requires2FA; // Thêm field này để báo cần xác thực 2FA
}