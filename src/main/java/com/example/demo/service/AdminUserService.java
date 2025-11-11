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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    public UserAdminResponse createUser(CreateUserRequest req) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        // Validate và parse gender
        User.Gender gender = parseGender(req.getGender());
        if (gender == null) {
            throw new RuntimeException("Giới tính không hợp lệ. Phải là: male, female hoặc other");
        }

        // Validate phone number
        if (req.getPhone() == null || req.getPhone().trim().isEmpty() || req.getPhone().length() != 10) {
            throw new RuntimeException("Số điện thoại phải có đúng 10 số");
        }

        // Validate dob
        if (req.getDob() == null) {
            throw new RuntimeException("Ngày sinh không được để trống");
        }

        // Validate address
        if (req.getAddress() == null || req.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ không được để trống");
        }

        // Validate bio
        if (req.getBio() == null || req.getBio().trim().isEmpty()) {
            throw new RuntimeException("Giới thiệu không được để trống");
        }
        if (req.getBio().length() > 1000) {
            throw new RuntimeException("Giới thiệu không được vượt quá 1000 ký tự");
        }

        // Tạo user mới
        User user = User.builder()
                .email(req.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .phone(req.getPhone().trim())
                .dob(req.getDob())
                .gender(gender)
                .address(req.getAddress().trim())
                .experienceYears(req.getExperienceYears())
                .photoUrl(req.getPhotoUrl() != null ? req.getPhotoUrl().trim() : null)
                .bio(req.getBio().trim())
                .isActive(true)
                .twoFactorEnabled(false)
                .otpCode(null)
                .otpExpiresAt(null)
                .build();

        // Lưu user vào database
        user = userRepository.save(user);

        // Xử lý roles - mặc định là "tiep_tan" nếu không có role nào được chỉ định
        List<String> roleNames = req.getRoles() != null && !req.getRoles().isEmpty() 
                ? req.getRoles() 
                : List.of("tiep_tan");
        
        // Sử dụng Set để tránh duplicate roles
        Set<String> uniqueRoleNames = new HashSet<>();
        List<String> savedRoleNames = new ArrayList<>();
        
        for (String roleName : roleNames) {
            String normalized = normalizeRoleName(roleName);
            
            // Kiểm tra role đã được xử lý chưa (tránh duplicate trong cùng request)
            if (uniqueRoleNames.contains(normalized)) {
                continue;
            }
            
            // Tìm role trong database
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName + ". Các role hợp lệ: admin, bac_si, tiep_tan"));
            
            // Tạo UserRole
            UserRole ur = UserRole.builder()
                    .user(user)
                    .role(role)
                    .build();
            userRoleRepository.save(ur);
            
            uniqueRoleNames.add(normalized);
            savedRoleNames.add(role.getName());
        }

        // Trả về thông tin user đầy đủ
        return buildUserAdminResponse(user, savedRoleNames);
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

    public UserAdminResponse updateUser(UUID userId, UpdateUserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Chỉ cập nhật các trường được cung cấp (không set null cho các trường NOT NULL)
        if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
            user.setFullName(req.getFullName().trim());
        }
        
        if (req.getPhone() != null && !req.getPhone().trim().isEmpty()) {
            if (req.getPhone().trim().length() != 10) {
                throw new RuntimeException("Số điện thoại phải có đúng 10 số");
            }
            user.setPhone(req.getPhone().trim());
        }
        
        if (req.getDob() != null && !req.getDob().trim().isEmpty()) {
            try {
                user.setDob(LocalDate.parse(req.getDob()));
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày sinh không hợp lệ");
            }
        }
        
        if (req.getGender() != null && !req.getGender().trim().isEmpty()) {
            User.Gender gender = parseGender(req.getGender());
            if (gender == null) {
                throw new RuntimeException("Giới tính không hợp lệ. Phải là: male, female hoặc other");
            }
            user.setGender(gender);
        }
        
        if (req.getAddress() != null && !req.getAddress().trim().isEmpty()) {
            user.setAddress(req.getAddress().trim());
        }
        
        if (req.getExperienceYears() != null) {
            user.setExperienceYears(req.getExperienceYears());
        }
        
        if (req.getPhotoUrl() != null) {
            user.setPhotoUrl(req.getPhotoUrl().trim());
        }
        
        if (req.getBio() != null && !req.getBio().trim().isEmpty()) {
            if (req.getBio().trim().length() > 1000) {
                throw new RuntimeException("Giới thiệu không được vượt quá 1000 ký tự");
            }
            user.setBio(req.getBio().trim());
        }
        
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