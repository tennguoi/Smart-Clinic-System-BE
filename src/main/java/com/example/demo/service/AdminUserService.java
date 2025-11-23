package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UserAdminResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    public UserAdminResponse createUser(CreateUserRequest req, MultipartFile photo) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Lưu ảnh nếu có
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = fileStorageService.savePhoto(photo);
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .dob(req.getDob())
                .gender(parseGender(req.getGender()))
                .address(req.getAddress())
                .experienceYears(req.getExperienceYears())
                .photoUrl(photoUrl)
                .bio(req.getBio())
                .isVerified(req.getIsVerified() != null ? req.getIsVerified() : true) // Mặc định là chưa xác thực
                .twoFactorEnabled(false)
                .build();
        user = userRepository.save(user);

        List<String> roleNames = req.getRoles() != null ? req.getRoles() : List.of("tiếp tân");
        List<String> savedRoleNames = new ArrayList<>();
        for (String roleName : roleNames) {
            String normalized = normalizeRoleName(roleName);
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));
            UserRole ur = UserRole.builder().user(user).role(role).build();
            userRoleRepository.save(ur);
            savedRoleNames.add(role.getName());
        }

        return UserAdminResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(savedRoleNames)
                .isVerified(user.getIsVerified())
                .build();
    }

    public UserAdminResponse updateUserRoles(UUID userId, UpdateUserRolesRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<UserRole> current = userRoleRepository.findByUser_UserId(userId);
        userRoleRepository.deleteAll(current);

        List<String> savedRoleNames = new ArrayList<>();
        for (String roleName : req.getRoles()) {
            String normalized = normalizeRoleName(roleName);
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));
            UserRole ur = UserRole.builder().user(user).role(role).build();
            userRoleRepository.save(ur);
            savedRoleNames.add(role.getName());
        }

        return UserAdminResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(savedRoleNames)
                .isVerified(user.getIsVerified())
                .build();
    }

    public List<UserAdminResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                    .stream()
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList());
            return buildUserAdminResponse(user, roleNames);
        }).collect(Collectors.toList());
    }

    public UserAdminResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        List<String> roleNames = userRoleRepository.findByUser_UserId(userId)
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());
        return buildUserAdminResponse(user, roleNames);
    }


    public UserAdminResponse updateUser(UUID userId, UpdateUserRequest req, MultipartFile photo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Cập nhật thông tin cơ bản
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }

        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }

        if (req.getAddress() != null) {
            user.setAddress(req.getAddress());
        }

        if (req.getExperienceYears() != null) {
            user.setExperienceYears(req.getExperienceYears());
        }

        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }

        // Xử lý ngày sinh
        if (req.getDob() != null && !req.getDob().trim().isEmpty()) {
            try {
                // Nếu format từ frontend là dd/MM/yyyy
                if (req.getDob().contains("/")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    user.setDob(LocalDate.parse(req.getDob(), formatter));
                } else {
                    // Nếu format là yyyy-MM-dd
                    user.setDob(LocalDate.parse(req.getDob()));
                }
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày sinh không hợp lệ");
            }
        }

        // Xử lý giới tính
        if (req.getGender() != null) {
            user.setGender(parseGender(req.getGender()));
        }

        // Cập nhật mật khẩu nếu có
        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        // Xử lý ảnh đại diện
        if (photo != null && !photo.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deletePhoto(user.getPhotoUrl());
                } catch (Exception e) {
                    // Log lỗi nhưng không throw exception
                    System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                }
            }
            // Lưu ảnh mới
            String photoUrl = fileStorageService.savePhoto(photo);
            user.setPhotoUrl(photoUrl);
        }

        // Lưu user
        user = userRepository.save(user);

        // Lấy danh sách role để trả về
        List<String> roleNames = userRoleRepository.findByUser_UserId(userId)
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return buildUserAdminResponse(user, roleNames);
    }


    /**
     * Đổi trạng thái xác thực của user
     * @param userId - ID của user
     * @param isVerified - Trạng thái mới (true/false)
     * @return UserAdminResponse
     */
    public UserAdminResponse toggleVerifyStatus(UUID userId, Boolean isVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setIsVerified(isVerified);
        user = userRepository.save(user);

        List<String> roleNames = userRoleRepository.findByUser_UserId(userId)
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return buildUserAdminResponse(user, roleNames);
    }

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Xóa ảnh nếu có
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            fileStorageService.deletePhoto(user.getPhotoUrl());
        }

        userRoleRepository.deleteAll(userRoleRepository.findByUser_UserId(userId));
        userRepository.delete(user);
    }

    private UserAdminResponse buildUserAdminResponse(User user, List<String> roleNames) {
        return UserAdminResponse.builder()
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
    }

    private User.Gender parseGender(String gender) {
        if (gender == null) return null;
        try {
            return User.Gender.valueOf(gender.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalizeRoleName(String input) {
        if (input == null) return null;
        return input.trim().toLowerCase(Locale.ROOT);
    }
}