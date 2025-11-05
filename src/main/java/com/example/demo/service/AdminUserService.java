package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
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

    public UserAdminResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
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
                .photoUrl(req.getPhotoUrl())
                .bio(req.getBio())
                .isVerified(true)
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
