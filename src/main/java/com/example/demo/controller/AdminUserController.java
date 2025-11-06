package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.service.AdminUserService;
import com.example.demo.service.storage.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<UserAdminResponse> createUser(@RequestPart("user") @Valid CreateUserRequest request,
                                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.save(file);
            request.setPhotoUrl(filename);
        }
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserAdminResponse> updateRoles(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserRoles(userId, request));
    }
}
