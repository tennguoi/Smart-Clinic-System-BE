package com.example.demo.controller;


import com.example.demo.dto.LogoutResponse;
import com.example.demo.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        LogoutResponse response = authService.logout(token);
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