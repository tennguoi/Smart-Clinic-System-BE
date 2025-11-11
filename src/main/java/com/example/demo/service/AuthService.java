package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import com.example.demo.util.JwtUtil;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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

    @Value("${app.otp.expiry:5}")
    private int otpExpiryMinutes;

    // Tạo OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Lưu OTP vào User entity
    private void saveOtp(User user, String otpCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(otpExpiryMinutes);
        
        user.setOtpCode(otpCode);
        user.setOtpExpiresAt(expiresAt);
        userRepository.save(user);
    }

    // Kiểm tra OTP có hợp lệ không
    private boolean isValidOtp(User user, String otpCode) {
        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return user.getOtpCode().equals(otpCode) && user.getOtpExpiresAt().isAfter(now);
    }

    // Xóa OTP sau khi sử dụng
    private void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);
    }

    // Bật 2FA
    public GenericResponse enable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố đã được bật!");
        }
        // Tạo và gửi OTP, NHƯNG CHƯA BẬT 2FA
        String otp = generateOtp();
        saveOtp(user, otp);

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
            if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
                return new GenericResponse(false, "2FA is already disabled.");
            }
            user.setTwoFactorEnabled(false);
            clearOtp(user);
            return new GenericResponse(true, "2FA disabled successfully.");
        } catch (Exception e) {
            return new GenericResponse(false, "Failed to disable 2FA: " + e.getMessage());
        }
    }

    // Xác thực OTP khi đăng nhập với 2FA
    public GenericResponse verify2FA(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố chưa được bật!");
        }
        
        // Kiểm tra OTP
        if (!isValidOtp(user, request.getOtpCode())) {
            return new GenericResponse(false, "OTP không hợp lệ hoặc đã hết hạn!");
        }
        
        // Xóa OTP sau khi xác thực
        clearOtp(user);
        
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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        String otp = generateOtp();
        saveOtp(user, otp);
        emailService.sendOtpEmail(request.getEmail(), otp);
        return new GenericResponse(true, "OTP đã được gửi đến email của bạn!");
    }

    // Xác thực OTP cho quên mật khẩu
    public GenericResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        
        // Kiểm tra OTP
        if (!isValidOtp(user, request.getOtpCode())) {
            return new GenericResponse(false, "OTP không hợp lệ hoặc đã hết hạn!");
        }
        
        // Xóa OTP sau khi xác thực
        clearOtp(user);
        
        user.setIsActive(true);
        if (user.getTwoFactorEnabled() == null) {
            user.setTwoFactorEnabled(false);
        }
        userRepository.save(user);
        return new GenericResponse(true, "OTP xác thực thành công!");
    }

    // Đặt lại mật khẩu
    public GenericResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));
        if (!user.getIsActive()) {
            return new GenericResponse(false, "Vui lòng xác thực OTP trước!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setIsActive(true);
        if (user.getTwoFactorEnabled() == null) {
            user.setTwoFactorEnabled(false);
        }
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
            // Kiểm tra 2FA
            if (user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled()) {
                String otp = generateOtp();
                saveOtp(user, otp);
                emailService.sendOtpEmail(user.getEmail(), otp);
                return LoginResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .message("2FA required. OTP sent to your email.")
                        .build();
            }
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
        if (user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled()) {
            return new GenericResponse(false, "Xác thực hai yếu tố đã được bật!");
        }

        // Kiểm tra OTP
        if (!isValidOtp(user, request.getOtpCode())) {
            return new GenericResponse(false, "OTP không hợp lệ hoặc đã hết hạn!");
        }

        // Xóa OTP và BẬT 2FA
        clearOtp(user);
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
            
            // Chỉ cập nhật các trường được cung cấp (không set null cho các trường NOT NULL)
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                user.setFullName(request.getFullName().trim());
            }
            
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                if (request.getPhone().trim().length() != 10) {
                    return new GenericResponse(false, "Số điện thoại phải có đúng 10 số");
                }
                user.setPhone(request.getPhone().trim());
            }
            
            if (request.getDob() != null && !request.getDob().trim().isEmpty()) {
                try {
                    user.setDob(LocalDate.parse(request.getDob()));
                } catch (Exception e) {
                    return new GenericResponse(false, "Định dạng ngày sinh không hợp lệ");
                }
            }
            
            if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
                try {
                    User.Gender gender = User.Gender.valueOf(request.getGender().toLowerCase(Locale.ROOT));
                    user.setGender(gender);
                } catch (IllegalArgumentException e) {
                    return new GenericResponse(false, "Giới tính không hợp lệ. Phải là: male, female hoặc other");
                }
            }
            
            if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
                user.setAddress(request.getAddress().trim());
            }
            
            if (request.getExperienceYears() != null) {
                user.setExperienceYears(request.getExperienceYears());
            }
            
            if (request.getPhotoUrl() != null) {
                user.setPhotoUrl(request.getPhotoUrl().trim());
            }
            
            if (request.getBio() != null && !request.getBio().trim().isEmpty()) {
                if (request.getBio().trim().length() > 1000) {
                    return new GenericResponse(false, "Giới thiệu không được vượt quá 1000 ký tự");
                }
                user.setBio(request.getBio().trim());
            }
            
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