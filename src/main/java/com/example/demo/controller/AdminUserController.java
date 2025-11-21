package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.service.AdminUserService;
import com.example.demo.util.FileUploadUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createUser(
            @Valid @RequestPart("data") CreateUserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile) {
        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                String savedFileName = FileUploadUtil.saveFile("uploads", fileName, multipartFile);
                request.setPhotoUrl("/uploads/" + savedFileName);
            }
            
            UserAdminResponse response = adminUserService.createUser(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new com.example.demo.dto.GenericResponse(false, "Could not upload image: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new com.example.demo.dto.GenericResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserAdminResponse> updateRoles(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserRoles(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateUser(
            @PathVariable("userId") UUID userId,
            @Valid @RequestPart("data") UpdateUserRequest request,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile) {
        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                String savedFileName = FileUploadUtil.saveFile("uploads", fileName, multipartFile);
                request.setPhotoUrl("/uploads/" + savedFileName);
            }

            return ResponseEntity.ok(adminUserService.updateUser(userId, request));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new com.example.demo.dto.GenericResponse(false, "Could not upload image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}