package com.example.demo.repository;

import com.example.demo.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser_UserId(UUID userId);
    List<UserRole> findByRole_NameIgnoreCase(String roleName);
}

