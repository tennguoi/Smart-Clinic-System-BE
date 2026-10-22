package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BAC_SI')")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String photoUrl = fileStorageService.savePhoto(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", photoUrl);
            response.put("message", "Upload thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}