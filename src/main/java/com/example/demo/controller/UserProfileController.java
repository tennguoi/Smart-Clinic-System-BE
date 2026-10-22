package com.example.demo.controller;

import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import com.example.demo.service.AdminUserService;
import com.example.demo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho phép người dùng quản lý thông tin cá nhân của họ
 * Không yêu cầu quyền ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Lấy thông tin cá nhân của người dùng hiện tại
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserAdminResponse> getCurrentUserProfile() {
        try {
            // Lấy thông tin người dùng từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Tìm user theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Lấy roles của user
            List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                    .stream()
                    .map(ur -> {
                        try {
                            return ur.getRole().getName();
                        } catch (Exception e) {
                            System.err.println("Error getting role name: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(name -> name != null)
                    .collect(Collectors.toList());

            // Build response
            UserAdminResponse response = UserAdminResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .dob(user.getDob() != null ? user.getDob().toString() : null)
                    .gender(user.getGender() != null ? user.getGender().toString() : null)
                    .address(user.getAddress())
                    .experienceYears(user.getExperienceYears())
                    .photoUrl(user.getPhotoUrl())
                    .bio(user.getBio())
                    .roles(roleNames)
                    .isVerified(user.getIsVerified())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching current user profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể lấy thông tin cá nhân: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin cá nhân của người dùng
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserAdminResponse> updateCurrentUserProfile(
            @RequestPart("data") UpdateUserRequest req,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            // Lấy thông tin người dùng từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Tìm user theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Gọi service để cập nhật user (không cho phép thay đổi roles hoặc isVerified)
            UpdateUserRequest updateReq = new UpdateUserRequest();
            updateReq.setFullName(req.getFullName());
            updateReq.setPhone(req.getPhone());
            updateReq.setDob(req.getDob());
            updateReq.setGender(req.getGender());
            updateReq.setAddress(req.getAddress());
            updateReq.setExperienceYears(req.getExperienceYears());
            updateReq.setBio(req.getBio());

            UserAdminResponse response = adminUserService.updateUserFully(user.getUserId(), updateReq, photo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật thông tin cá nhân: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh đại diện
     * POST /api/users/profile/upload-photo
     */
    @PostMapping("/profile/upload-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo) {
        try {
            // Lấy thông tin người dùng từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Tìm user theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Xóa ảnh cũ nếu có
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deletePhoto(user.getPhotoUrl());
                } catch (Exception ignored) {
                }
            }

            // Lưu ảnh mới
            String photoUrl = fileStorageService.savePhoto(photo);
            user.setPhotoUrl(photoUrl);
            userRepository.save(user);

            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("success", true);
                put("data", photoUrl);
                put("message", "Cập nhật ảnh đại diện thành công");
            }});
        } catch (Exception e) {
            System.err.println("Error uploading photo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tải ảnh lên: " + e.getMessage());
        }
    }
}
