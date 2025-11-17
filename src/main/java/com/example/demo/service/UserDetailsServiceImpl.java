package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Xử lý isVerified: nếu null hoặc false thì disable, chỉ enable khi isVerified = true
        boolean isVerified = user.getIsVerified() != null && user.getIsVerified();
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!isVerified)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(role -> {
                    String name = role.getName();
                    String normalized = name == null ? "" : name.trim();
                    // Avoid double prefixing; ensure Spring-style "ROLE_*"
                    if (!normalized.toUpperCase().startsWith("ROLE_")) {
                        normalized = "ROLE_" + normalized.toUpperCase();
                    } else {
                        normalized = normalized.toUpperCase();
                    }
                    return new SimpleGrantedAuthority(normalized);
                })
                .collect(Collectors.toList());
    }
}
