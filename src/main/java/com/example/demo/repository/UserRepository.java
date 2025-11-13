package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying
    @Query("UPDATE User u SET u.otpCode = :otpCode, u.isVerified = false, u.twoFactorEnabled = true WHERE u.email = :email")
    void updateOtpForEmail(@Param("email") String email, @Param("otpCode") String otpCode);
}
