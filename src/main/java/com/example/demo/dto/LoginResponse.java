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
    private Boolean requires2FA; // Flag để báo frontend cần chuyển đến trang verify 2FA

    // Constructor cho trường hợp lỗi
    public LoginResponse(UUID userId, String email, String fullName, String token, List<String> roles, String message) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.roles = roles;
        this.message = message;
        this.requires2FA = false;
    }
}