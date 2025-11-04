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
import java.util.List;
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

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);

        return new GenericResponse(true, "OTP đã được gửi đến email của bạn để bật 2FA!");
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
            return new GenericResponse(false, "OTP không hợp lệ!");
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

            // Kiểm tra 2FA
            if (user.getTwoFactorEnabled()) {
                String otp = generateOtp();
                user.setOtpCode(otp);
                userRepository.save(user);
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
}