package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", length = 36) // Đã có length = 36
    private UUID userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 10)
    @NotNull
    private String phone;

    @Column(name = "dob", nullable = false)
    @NotNull
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Gender gender;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    private String address;

    // Sửa: Cho phép NULL (chỉ bắt buộc cho Bác sĩ)
    @Column(name = "experience_years")
    private Integer experienceYears;

    // Sửa: Cho phép NULL (chỉ bắt buộc cho Bác sĩ)
    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    // Sửa: Bỏ nullable=false và @NotNull, columnDefinition TEXT phù hợp hơn
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    public enum Gender {
        male, female, other
    }
}