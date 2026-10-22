package com.example.demo.controller.admin;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * Tạo user mới
     * @param req - Thông tin user (JSON)
     * @param photo - File ảnh (optional)
     */
    @PostMapping
    public ResponseEntity<UserAdminResponse> createUser(
            @RequestPart("data") CreateUserRequest req,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        UserAdminResponse response = adminUserService.createUser(req, photo);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách tất cả users với phân trang
     * GET /api/admin/users?page=0&size=8
     */
    @GetMapping
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<UserAdminResponse> users = adminUserService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy thông tin user theo ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable UUID userId) {
        try {
            UserAdminResponse user = adminUserService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("Error fetching user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Cập nhật thông tin user
     * @param userId - ID của user
     * @param req - Thông tin cập nhật (JSON)
     * @param photo - File ảnh mới (optional)
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
            @Valid @RequestBody UpdateUserRolesRequest req) {
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

    // ==================== CÁC ENDPOINT TÌM KIẾM CÓ PHÂN TRANG ====================

    /**
     * Tìm kiếm user theo tên, số điện thoại, hoặc email với phân trang
     * GET /api/admin/users/search/name-phone?keyword=...&page=0&size=8
     */
    @GetMapping("/search/name-phone")
    public ResponseEntity<Page<UserAdminResponse>> searchByNameOrPhone(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<UserAdminResponse> users = adminUserService.searchUsersByNameOrPhone(keyword, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm user theo vai trò với phân trang
     * GET /api/admin/users/search/role?roleName=...&page=0&size=8
     */
    @GetMapping("/search/role")
    public ResponseEntity<Page<UserAdminResponse>> searchByRole(
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<UserAdminResponse> users = adminUserService.searchUsersByRole(roleName, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm user theo trạng thái hoạt động với phân trang
     * GET /api/admin/users/search/verified?isVerified=true&page=0&size=8
     */
    @GetMapping("/search/verified")
    public ResponseEntity<Page<UserAdminResponse>> searchByVerifiedStatus(
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<UserAdminResponse> users = adminUserService.searchUsersByVerifiedStatus(isVerified, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm user theo giới tính với phân trang
     * GET /api/admin/users/search/gender?gender=male&page=0&size=8
     */
    @GetMapping("/search/gender")
    public ResponseEntity<Page<UserAdminResponse>> searchByGender(
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<UserAdminResponse> users = adminUserService.searchUsersByGender(gender, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm user với nhiều tiêu chí kết hợp và phân trang
     * keyword: tìm theo tên, số điện thoại, hoặc email
     * GET /api/admin/users/search?keyword=...&roleName=...&isVerified=...&gender=...&page=0&size=8
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserAdminResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<UserAdminResponse> users = adminUserService.searchUsers(keyword, roleName, isVerified, gender, page, size);
        return ResponseEntity.ok(users);
    }
}