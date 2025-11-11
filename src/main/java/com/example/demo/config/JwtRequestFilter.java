package com.example.demo.config;

import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // Danh sách các đường dẫn CÔNG KHAI - KHÔNG cần JWT
    private static final String[] PUBLIC_PATHS = {
            "/api/reviews/",
            "/api/auth/",
            "/api/public/",
            "/h2-console/",
            "/swagger-ui/",
            "/v3/api-docs/"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(PUBLIC_PATHS)
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // BỎ QUA nếu là route công khai → không kiểm tra token
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        logger.info("Authorization header: " + requestTokenHeader);

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getEmailFromToken(jwtToken);
                logger.info("Username from token: " + username);
            } catch (Exception e) {
                // Không log lỗi nếu không có token (bình thường với route bảo vệ)
                // logger.warn("JWT Token invalid or expired");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            logger.info("User authorities: " + userDetails.getAuthorities());

            if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
                logger.info("Token is valid");
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                logger.warn("Token validation failed");
            }
        }

        chain.doFilter(request, response);
    }
}