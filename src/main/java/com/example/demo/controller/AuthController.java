package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null, null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        LogoutResponse response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        GenericResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<GenericResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        GenericResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        GenericResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<GenericResponse> enable2FA(@RequestBody ForgotPasswordRequest request) {
        GenericResponse response = authService.enable2FA(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<GenericResponse> verify2FA(@Valid @RequestBody OtpVerificationRequest request) {
        GenericResponse response = authService.verify2FA(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/403")
    public ResponseEntity<String> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền truy cập vào tính năng này");
    }

    @GetMapping("/401")
    public ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn không có quyền truy cập vào tính năng này");
    }

    @GetMapping("/500")
    public ResponseEntity<String> internalServerError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server");
    }
}