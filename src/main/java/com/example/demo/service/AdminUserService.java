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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
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
                .isVerified(req.getIsVerified() != null ? req.getIsVerified() : true)
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

    /**
     * Lấy danh sách tất cả users với phân trang
     */
    public Page<UserAdminResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserAdminResponse> responses = userPage.getContent().stream()
                .map(user -> {
                    List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                            .stream()
                            .map(ur -> ur.getRole().getName())
                            .collect(Collectors.toList());
                    return buildUserAdminResponse(user, roleNames);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, userPage.getTotalElements());
    }

    /**
     * Tìm kiếm user theo tên hoặc số điện thoại với phân trang
     * - Hỗ trợ tìm không dấu tiếng Việt cho tên
     */
    public Page<UserAdminResponse> searchUsersByNameOrPhone(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(page, size);
        }

        String normalizedKeyword = removeAccents(keyword.trim());

        List<User> allUsers = userRepository.findAll().stream()
                .filter(user -> {
                    boolean matchName = user.getFullName() != null &&
                            removeAccents(user.getFullName()).contains(normalizedKeyword);
                    boolean matchPhone = user.getPhone() != null &&
                            user.getPhone().contains(keyword.trim());
                    return matchName || matchPhone;
                })
                .collect(Collectors.toList());

        return convertToPage(allUsers, page, size);
    }

    /**
     * Tìm kiếm user theo vai trò với phân trang
     * - Cho phép truyền role bằng chữ có/không dấu (normalize trước khi tìm)
     */
    public Page<UserAdminResponse> searchUsersByRole(String roleName, int page, int size) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return getAllUsers(page, size);
        }

        String normalized = normalizeRoleName(roleName);
        Role role = roleRepository.findByName(normalized)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));

        List<UserRole> userRoles = userRoleRepository.findByRole_RoleId(role.getRoleId());
        List<User> users = userRoles.stream()
                .map(UserRole::getUser)
                .collect(Collectors.toList());

        return convertToPage(users, page, size);
    }

    /**
     * Tìm kiếm user theo trạng thái hoạt động (isVerified) với phân trang
     */
    public Page<UserAdminResponse> searchUsersByVerifiedStatus(Boolean isVerified, int page, int size) {
        if (isVerified == null) {
            return getAllUsers(page, size);
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> isVerified.equals(user.getIsVerified()))
                .collect(Collectors.toList());

        return convertToPage(users, page, size);
    }

    /**
     * Tìm kiếm user theo giới tính với phân trang
     */
    public Page<UserAdminResponse> searchUsersByGender(String genderStr, int page, int size) {
        if (genderStr == null || genderStr.trim().isEmpty()) {
            return getAllUsers(page, size);
        }

        User.Gender gender = parseGender(genderStr);
        if (gender == null) {
            throw new RuntimeException("Giới tính không hợp lệ: " + genderStr);
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> gender.equals(user.getGender()))
                .collect(Collectors.toList());

        return convertToPage(users, page, size);
    }

    /**
     * Tìm kiếm user với nhiều tiêu chí kết hợp và phân trang
     * - keyword: hỗ trợ tìm không dấu trên tên
     * - roleName: hỗ trợ tìm role không dấu
     */
    public Page<UserAdminResponse> searchUsers(String keyword, String roleName,
                                               Boolean isVerified, String genderStr,
                                               int page, int size) {
        List<User> users = userRepository.findAll();

        // Lọc theo keyword (tên hoặc số điện thoại)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String normalizedKeyword = removeAccents(keyword.trim());
            users = users.stream()
                    .filter(user -> {
                        boolean matchName = user.getFullName() != null &&
                                removeAccents(user.getFullName()).contains(normalizedKeyword);
                        boolean matchPhone = user.getPhone() != null &&
                                user.getPhone().contains(keyword.trim());
                        return matchName || matchPhone;
                    })
                    .collect(Collectors.toList());
        }

        // Lọc theo trạng thái
        if (isVerified != null) {
            users = users.stream()
                    .filter(user -> isVerified.equals(user.getIsVerified()))
                    .collect(Collectors.toList());
        }

        // Lọc theo giới tính
        if (genderStr != null && !genderStr.trim().isEmpty()) {
            User.Gender gender = parseGender(genderStr);
            if (gender != null) {
                users = users.stream()
                        .filter(user -> gender.equals(user.getGender()))
                        .collect(Collectors.toList());
            }
        }

        // Lọc theo vai trò
        if (roleName != null && !roleName.trim().isEmpty()) {
            String normalized = normalizeRoleName(roleName);
            Role role = roleRepository.findByName(normalized).orElse(null);
            if (role != null) {
                List<UUID> userIdsWithRole = userRoleRepository.findByRole_RoleId(role.getRoleId())
                        .stream()
                        .map(ur -> ur.getUser().getUserId())
                        .collect(Collectors.toList());
                users = users.stream()
                        .filter(user -> userIdsWithRole.contains(user.getUserId()))
                        .collect(Collectors.toList());
            } else {
                return Page.empty(PageRequest.of(page, size));
            }
        }

        return convertToPage(users, page, size);
    }

    /**
     * Chuyển đổi List<User> thành Page<UserAdminResponse>
     */
    private Page<UserAdminResponse> convertToPage(List<User> users, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        List<UserAdminResponse> responses = users.subList(start, end).stream()
                .map(user -> {
                    List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                            .stream()
                            .map(ur -> ur.getRole().getName())
                            .collect(Collectors.toList());
                    return buildUserAdminResponse(user, roleNames);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, users.size());
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        List<String> roleNames = new ArrayList<>();
        try {
            // Fetch user roles with eager loading
            List<UserRole> userRoles = userRoleRepository.findByUser_UserId(userId);
            roleNames = userRoles.stream()
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
        } catch (Exception e) {
            // If there's an error fetching roles, just return empty list
            System.err.println("Error fetching roles for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return buildUserAdminResponse(user, roleNames);
    }

    @Transactional
    public UserAdminResponse updateUserFully(UUID userId, UpdateUserRequest req, MultipartFile photo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getExperienceYears() != null) user.setExperienceYears(req.getExperienceYears());
        if (req.getBio() != null) user.setBio(req.getBio());

        if (req.getDob() != null && !req.getDob().trim().isEmpty()) {
            try {
                LocalDate parsed = req.getDob().contains("/")
                        ? LocalDate.parse(req.getDob(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : LocalDate.parse(req.getDob());
                user.setDob(parsed);
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày sinh không hợp lệ");
            }
        }

        if (req.getGender() != null) {
            user.setGender(parseGender(req.getGender()));
        }
        if (req.getTwoFactorEnabled() != null) {
            user.setTwoFactorEnabled(req.getTwoFactorEnabled());
        }
        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getIsVerified() != null) {
            user.setIsVerified(req.getIsVerified());
        }

        if (photo != null && !photo.isEmpty()) {
            if (user.getPhotoUrl() != null) {
                try { fileStorageService.deletePhoto(user.getPhotoUrl()); }
                catch (Exception ignored) {}
            }
            user.setPhotoUrl(fileStorageService.savePhoto(photo));
        }

        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            List<UserRole> oldRoles = userRoleRepository.findByUser_UserId(userId);
            if (!oldRoles.isEmpty()) {
                userRoleRepository.deleteAll(oldRoles);
            }

            for (String roleName : req.getRoles()) {
                String normalized = normalizeRoleName(roleName);
                Role role = roleRepository.findByName(normalized)
                        .orElseThrow(() -> new RuntimeException("Role không tồn tại: " + roleName));
                userRoleRepository.save(UserRole.builder().user(user).role(role).build());
            }
        }

        user = userRepository.save(user);

        List<String> currentRoleNames = userRoleRepository.findByUser_UserId(userId)
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return buildUserAdminResponse(user, currentRoleNames);
    }

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

    /**
     * Parse gender safely (ignore case)
     */
    private User.Gender parseGender(String gender) {
        if (gender == null) return null;
        String normalized = gender.trim();
        for (User.Gender g : User.Gender.values()) {
            if (g.name().equalsIgnoreCase(normalized)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Loại bỏ dấu tiếng Việt và chuẩn hoá thành chữ thường, trim
     */
    private String removeAccents(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        // loại bỏ các combining diacritical marks
        String withoutAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    /**
     * Chuẩn hoá tên role để tìm (loại bỏ dấu, chuyển thường, trim)
     */
    private String normalizeRoleName(String input) {
        if (input == null) return null;
        return removeAccents(input);
    }
}
