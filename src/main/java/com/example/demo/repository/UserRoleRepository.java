package com.example.demo.repository;

import com.example.demo.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role WHERE ur.user.userId = :userId")
    List<UserRole> findByUser_UserId(@Param("userId") UUID userId);
    
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role WHERE ur.role.name = :roleName")
    List<UserRole> findByRoleName(@Param("roleName") String roleName);
}

