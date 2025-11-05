package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // Tạo người dùng mới
    @PostMapping("/create")
    public ResponseEntity<UserAdminResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    // Cập nhật vai trò người dùng
    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserAdminResponse> updateRoles(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserRoles(userId, request));
    }

    // Lấy danh sách tất cả người dùng
    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    // Lấy thông tin chi tiết của một người dùng
    @GetMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    // Cập nhật thông tin cá nhân của người dùng
    @PutMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    // Xóa người dùng
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}