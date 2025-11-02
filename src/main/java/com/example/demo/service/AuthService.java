package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.LogoutResponse;
import com.example.demo.entity.Role;
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

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

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
    // đơn giản xoa token đi thôi
    public LogoutResponse logout(String token) {
        try {
            
            SecurityContextHolder.clearContext();
            
            return new LogoutResponse("Logout successful", true);
        } catch (Exception e) {
            return new LogoutResponse("Logout failed: " + e.getMessage(), false);
        }
    }
}

