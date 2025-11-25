package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.dto.auth.*;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Tạo OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Bật 2FA
    public GenericResponse enable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố đã được bật!");
        }
        // Tạo và gửi OTP, NHƯNG CHƯA BẬT 2FA
        String otp = generateOtp();
        user.setOtpCode(otp);
        // KHÔNG SET twoFactorEnabled = true ở đây
        userRepository.save(user);

        // Gửi email
        try {
            emailService.sendOtpEmail(email, otp);
            System.out.println("OTP sent to email: " + email + " - OTP: " + otp); // Debug log
            return new GenericResponse(true, "OTP đã được gửi đến email của bạn để bật 2FA!");
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            return new GenericResponse(false, "Không thể gửi email. Vui lòng thử lại!");
        }
    }

    // Tắt 2FA
    public GenericResponse disable2FA(String token) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (!jwtUtil.validateToken(token, email)) {
                return new GenericResponse(false, "Invalid or expired token.");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));
            if (!user.getTwoFactorEnabled()) {
                return new GenericResponse(false, "2FA is already disabled.");
            }
            user.setTwoFactorEnabled(false);
            user.setOtpCode(null);
            userRepository.save(user);
            return new GenericResponse(true, "2FA disabled successfully.");
        } catch (Exception e) {
            return new GenericResponse(false, "Failed to disable 2FA: " + e.getMessage());
        }
    }

    // Xác thực OTP khi đăng nhập với 2FA
    public GenericResponse verify2FA(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (!user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố chưa được bật!");
        }
        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            return new GenericResponse(false, "OTP không hợp lệ!");
        }
        // Xóa OTP sau khi xác thực
        user.setOtpCode(null);
        userRepository.save(user);
        // Tạo JWT token sau khi xác thực OTP
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        List<UserRole> userRoles = userRoleRepository.findByUser_UserId(user.getUserId());
        List<String> roleNames = userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(user.getEmail(), roleNames, user.getUserId());
        return new GenericResponse(true, "Xác thực 2FA thành công!", token);
    }

    // Gửi OTP cho quên mật khẩu
    public GenericResponse sendOtp(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            return new GenericResponse(false, "Email không tồn tại!");
        }
        String otp = generateOtp();
        userRepository.updateOtpForEmail(request.getEmail(), otp);
        emailService.sendOtpEmail(request.getEmail(), otp);
        return new GenericResponse(true, "OTP đã được gửi đến email của bạn!");
    }

    // Xác thực OTP cho quên mật khẩu
    public GenericResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("OTP không hợp lệ!"); // ← Ném exception → 500 hoặc xử lý bằng @ExceptionHandler
        }

        user.setOtpCode(null);
        user.setIsVerified(true);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        return new GenericResponse(true, "OTP xác thực thành công!");
    }

    // Đặt lại mật khẩu
    public GenericResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (!user.getIsVerified()) {
            return new GenericResponse(false, "Vui lòng xác thực OTP trước!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setIsVerified(true);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        return new GenericResponse(true, "Mật khẩu đã được cập nhật thành công!");
    }

    // Đăng nhập
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Kiểm tra nếu 2FA được bật
            if (user.getTwoFactorEnabled()) {
                String otp = generateOtp();
                user.setOtpCode(otp);
                userRepository.save(user);
                emailService.sendOtpEmail(user.getEmail(), otp);

                // Trả về response với requires2FA = true
                return LoginResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .message("2FA required. OTP sent to your email.")
                        .requires2FA(true) // <-- QUAN TRỌNG: Flag để frontend biết cần verify
                        .build();
            }

            // Đăng nhập bình thường (không có 2FA)
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            List<UserRole> userRoles = userRoleRepository.findByUser_UserId(user.getUserId());
            List<String> roleNames = userRoles.stream()
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList());
            String token = jwtUtil.generateToken(user.getEmail(), roleNames, user.getUserId());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .token(token)
                    .roles(roleNames)
                    .message("Login successful")
                    .requires2FA(false) // Không cần verify 2FA
                    .build();
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }

    // Đăng xuất
    public LogoutResponse logout(String token) {
        try {
            SecurityContextHolder.clearContext();
            return new LogoutResponse("Logout successful", true);
        } catch (Exception e) {
            return new LogoutResponse("Logout failed: " + e.getMessage(), false);
        }
    }

    // Xác thực OTP để bật 2FA
    public GenericResponse verify2FAEnable(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        // Kiểm tra xem đã bật chưa
        if (user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố đã được bật!");
        }

        // Kiểm tra OTP
        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            return new GenericResponse(false, "OTP không hợp lệ!");
        }

        // XÓA OTP và BẬT 2FA
        user.setOtpCode(null);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        System.out.println("2FA enabled successfully for: " + request.getEmail()); // Debug log
        return new GenericResponse(true, "Xác thực 2FA đã được bật thành công!");
    }

    // Đổi mật khẩu
    public GenericResponse changePassword(String token, ChangePasswordRequest request) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (!jwtUtil.validateToken(token, email)) {
                return new GenericResponse(false, "Token không hợp lệ hoặc đã hết hạn.");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return new GenericResponse(false, "Mật khẩu hiện tại không đúng.");
            }
            if (request.getNewPassword().length() < 8) {
                return new GenericResponse(false, "Mật khẩu mới phải có ít nhất 8 ký tự.");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return new GenericResponse(true, "Mật khẩu đã được cập nhật thành công!");
        } catch (Exception e) {
            return new GenericResponse(false, "Đổi mật khẩu thất bại: " + e.getMessage());
        }
    }

    // Cập nhật hồ sơ
    public GenericResponse updateProfile(String token, UpdateUserRequest request) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (!jwtUtil.validateToken(token, email)) {
                return new GenericResponse(false, "Token không hợp lệ hoặc đã hết hạn.");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setDob(request.getDob() != null ? LocalDate.parse(request.getDob()) : null);
            user.setGender(request.getGender() != null ? User.Gender.valueOf(request.getGender().toLowerCase(Locale.ROOT)) : null);
            user.setAddress(request.getAddress());
            user.setExperienceYears(request.getExperienceYears());
            user.setPhotoUrl(request.getPhotoUrl());
            user.setBio(request.getBio());
            userRepository.save(user);
            return new GenericResponse(true, "Hồ sơ đã được cập nhật thành công.");
        } catch (Exception e) {
            return new GenericResponse(false, "Cập nhật hồ sơ thất bại: " + e.getMessage());
        }
    }

    // Lấy thông tin người dùng
    public UserAdminResponse getUser(String token) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (!jwtUtil.validateToken(token, email)) {
                throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn.");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
            List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                    .stream()
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList());
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
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy thông tin người dùng: " + e.getMessage());
        }
    }
}