package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone
    ) {
        List<UserAdminResponse> users = adminUserService.getAllUsers(keyword, phone);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserAdminResponse> createUser(
            @RequestPart("data") CreateUserRequest req,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        UserAdminResponse response = adminUserService.createUser(req, photo);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable UUID userId) {
        UserAdminResponse user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**

     * @param userId
     * @param req
     * @param photo
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable UUID userId,
            @RequestPart("data") UpdateUserRequest req,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        UserAdminResponse response = adminUserService.updateUserFully(userId, req, photo);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật roles của user
     */
    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserAdminResponse> updateUserRoles(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRolesRequest req) {
        UserAdminResponse response = adminUserService.updateUserRoles(userId, req);
        return ResponseEntity.ok(response);
    }

    /**
     * Đổi trạng thái xác thực của user (kích hoạt/vô hiệu hóa tài khoản)
     * @param userId - ID của user
     * @param request - Body chứa isVerified (true/false)
     * @return UserAdminResponse
     */
    @PatchMapping("/{userId}/verify-status")
    public ResponseEntity<UserAdminResponse> toggleVerifyStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isVerified = request.get("isVerified");
        if (isVerified == null) {
            throw new RuntimeException("isVerified không được để trống");
        }
        UserAdminResponse response = adminUserService.toggleVerifyStatus(userId, isVerified);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}