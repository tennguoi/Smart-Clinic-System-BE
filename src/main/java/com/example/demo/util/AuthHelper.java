package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    @Autowired
    private JwtUtil jwtUtil;

    public String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String validateAndGetEmail(String token) {
        if (token == null) {
            return null;
        }
        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (jwtUtil.validateToken(token, email)) {
                return email;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
