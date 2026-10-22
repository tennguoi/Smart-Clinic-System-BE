package com.example.demo.controller;

import com.example.demo.dto.SettingResponse;
import com.example.demo.dto.UpdateSettingRequest;
import com.example.demo.service.SettingService;
import com.example.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("isAuthenticated()")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get current user's settings
     */
    @GetMapping
    public ResponseEntity<SettingResponse> getSettings(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID userId = jwtUtil.getUserIdFromToken(token);
            SettingResponse settings = settingService.getOrCreateSetting(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Update current user's settings
     */
    @PutMapping
    public ResponseEntity<SettingResponse> updateSettings(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody UpdateSettingRequest request) {
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID userId = jwtUtil.getUserIdFromToken(token);
            SettingResponse settings = settingService.updateSetting(userId, request);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
