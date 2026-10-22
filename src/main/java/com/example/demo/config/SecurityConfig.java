package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})   // <---- THÊM DÒNG NÀY
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Public APIs
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/chatbot/**").permitAll()
                        .requestMatchers("/webhook/**").permitAll()

                        // 3. User cá nhân (Profile, Settings)
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/settings/**").authenticated()

                        // 4. Role ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // 5. Reception Rooms (Lễ tân + Bác sĩ + Admin cùng dùng)
                        .requestMatchers("/api/reception/rooms/**").hasAnyAuthority("ROLE_TIEP_TAN", "ROLE_BAC_SI", "ROLE_ADMIN")

                        // 6. Role TIEP_TAN & ADMIN
                        .requestMatchers("/api/reception/**").hasAnyAuthority("ROLE_TIEP_TAN", "ROLE_ADMIN")
                        .requestMatchers("/api/receptionist/**").hasAnyAuthority("ROLE_TIEP_TAN", "ROLE_ADMIN")
                        .requestMatchers("/api/appointments/**").hasAnyAuthority("ROLE_TIEP_TAN", "ROLE_ADMIN")

                        // 7. Role BAC_SI & ADMIN
                        .requestMatchers("/api/doctor/**").hasAnyAuthority("ROLE_BAC_SI", "ROLE_ADMIN")
                        .requestMatchers("/api/doctors/**").hasAnyAuthority("ROLE_BAC_SI", "ROLE_ADMIN")
                        .requestMatchers("/api/v1/tmh-assistant/**").hasAnyAuthority("ROLE_BAC_SI", "ROLE_ADMIN")

                        // 8. Shared medical & billing records (Chi tiết kiểm soát qua @PreAuthorize)
                        .requestMatchers("/api/medical-records/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_BAC_SI", "ROLE_TIEP_TAN")
                        .requestMatchers("/api/invoices/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_BAC_SI", "ROLE_TIEP_TAN")
                        .requestMatchers("/api/billing/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_BAC_SI", "ROLE_TIEP_TAN")

                        // 9. Tất cả các request còn lại phải được xác thực
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}